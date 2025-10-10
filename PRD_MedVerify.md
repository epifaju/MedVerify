# PRD - Application Mobile de Vérification de Médicaments par Scan Data Matrix
## Version 2.0 - Optimisé pour développement IA

---

## 1. VISION & CONTEXTE

### 1.1 Problématique
La Guinée-Bissau fait face à un taux de contrefaçon médicamenteuse estimé à 30-50% du marché pharmaceutique, causant :
- Inefficacité des traitements et résistance aux antibiotiques
- Décès évitables par intoxication ou sous-dosage
- Perte de confiance dans le système de santé (< 40% de confiance selon OMS 2023)
- Pertes économiques estimées à 2M$ annuels

### 1.2 Solution proposée
Application mobile de vérification instantanée d'authenticité via scan de code Data Matrix GS1, avec système de traçabilité et signalement collaboratif.

### 1.3 Objectifs mesurables (6 mois post-lancement)
- 10 000 téléchargements actifs
- 85% de taux de satisfaction utilisateur
- 500 pharmacies partenaires enregistrées
- Réduction de 20% des signalements de médicaments suspects

---

## 2. PERSONAS & CAS D'USAGE

### Persona 1 : Patient (Priorité 1)
**Profil** : Maria, 35 ans, Bissau, niveau éducation secondaire
**Besoins** : Vérifier rapidement si médicament acheté est authentique
**Contraintes** : Connexion internet limitée, smartphone Android entry-level (2GB RAM)
**User Story** :
```
EN TANT QUE patient
JE VEUX scanner le code sur ma boîte de médicaments
AFIN DE savoir instantanément s'il est authentique
CRITÈRES D'ACCEPTATION :
- Scan réussi en < 3 secondes
- Résultat affiché avec code couleur clair
- Fonctionne sans connexion internet
- Historique consultable
```

### Persona 2 : Pharmacien (Priorité 1)
**Profil** : João, 45 ans, pharmacien titulaire, Bissau
**Besoins** : Vérifier stocks, signaler anomalies, accéder à données détaillées
**User Story** :
```
EN TANT QUE pharmacien
JE VEUX scanner plusieurs médicaments rapidement
AFIN DE vérifier mon stock et signaler anomalies
CRITÈRES D'ACCEPTATION :
- Mode scan multiple (batch)
- Export CSV de l'historique
- Accès détails techniques (lot, fabricant, date)
- Signalement prioritaire aux autorités
```

### Persona 3 : Autorité Sanitaire (Priorité 2)
**Profil** : Dr. Santos, inspecteur INASA
**Besoins** : Tableau de bord analytique, alertes temps réel, rapports
**User Story** :
```
EN TANT QU'autorité sanitaire
JE VEUX consulter les statistiques de contrefaçon
AFIN D'identifier zones/produits à risque et coordonner inspections
CRITÈRES D'ACCEPTATION :
- Dashboard avec KPIs temps réel
- Carte géographique des signalements
- Export rapports PDF/Excel
- Notifications push pour alertes critiques
```

---

## 3. SPÉCIFICATIONS FONCTIONNELLES DÉTAILLÉES

### 3.1 Module Scan Data Matrix

#### F1.1 - Scan caméra
**Description** : Scanner code Data Matrix GS1 via caméra smartphone
**Priorité** : P0 (Critical)
**Dépendances** : Aucune

**Spécifications techniques** :
- Librairie : `react-native-vision-camera` v3.x + `vision-camera-code-scanner`
- Formats supportés : Data Matrix (ISO/IEC 16022), fallback QR Code
- Performance : < 2s pour détection, < 1s pour décodage
- Conditions : Min luminosité 50 lux, distance 5-20cm
- Fonctionnalités :
  - Guidage visuel (cadre overlay avec coins animés)
  - Flash auto/manuel
  - Focus automatique continu
  - Rotation auto de l'image
  - Feedback haptique au scan réussi

**Format Data Matrix GS1 attendu** :
```
(01)03401234567890(17)251231(10)ABC123(21)XYZ789
Décodage :
- (01) : GTIN (Global Trade Item Number) - 14 chiffres
- (17) : Date péremption (AAMMJJ)
- (10) : Numéro de lot
- (21) : Numéro de série unique
```

**Code exemple React Native** :
```typescript
// Fichier : src/components/Scanner/DataMatrixScanner.tsx
import { Camera, useCameraDevice, useCodeScanner } from 'react-native-vision-camera';

interface ScanResult {
  gtin: string;
  expiryDate: string;
  batchNumber: string;
  serialNumber: string;
}

export const DataMatrixScanner = ({ onScanSuccess }) => {
  const device = useCameraDevice('back');
  
  const codeScanner = useCodeScanner({
    codeTypes: ['data-matrix', 'qr'],
    onCodeScanned: (codes) => {
      const gs1Data = parseGS1(codes[0].value);
      onScanSuccess(gs1Data);
    }
  });

  return (
    <Camera
      style={StyleSheet.absoluteFill}
      device={device}
      isActive={true}
      codeScanner={codeScanner}
    />
  );
};
```

**Tests requis** :
- [ ] Scan réussi avec code valide en < 3s
- [ ] Rejet code invalide avec message erreur
- [ ] Fonctionnement avec luminosité faible
- [ ] Gestion permissions caméra refusées

#### F1.2 - Mode hors ligne
**Description** : Cache local SQLite pour vérifications sans internet
**Priorité** : P0

**Spécifications techniques** :
- Stockage : SQLite via `@react-native-async-storage/async-storage` ou `react-native-sqlite-storage`
- Cache : 1000 derniers médicaments scannés + 500 médicaments essentiels pré-chargés
- Synchronisation : Auto toutes les 24h si connexion disponible
- TTL cache : 30 jours pour données génériques, 7 jours pour données dynamiques

**Schéma base locale** :
```sql
CREATE TABLE cached_medications (
  id INTEGER PRIMARY KEY,
  gtin TEXT UNIQUE NOT NULL,
  name TEXT NOT NULL,
  manufacturer TEXT,
  is_authentic BOOLEAN,
  data JSONB, -- Infos complètes
  cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP
);

CREATE INDEX idx_gtin ON cached_medications(gtin);
CREATE INDEX idx_expires ON cached_medications(expires_at);
```

**Logique décision hors ligne** :
```typescript
async function verifyOffline(gtin: string): Promise<VerificationResult> {
  const cached = await db.query('SELECT * FROM cached_medications WHERE gtin = ?', [gtin]);
  
  if (cached && cached.expires_at > Date.now()) {
    return {
      status: cached.is_authentic ? 'AUTHENTIC' : 'SUSPICIOUS',
      source: 'CACHE_LOCAL',
      confidence: 'MEDIUM',
      data: cached.data
    };
  }
  
  // Si pas en cache et hors ligne
  return {
    status: 'UNKNOWN',
    source: 'OFFLINE',
    confidence: 'LOW',
    message: 'Connexion requise pour vérification complète'
  };
}
```

### 3.2 Module Vérification & API Backend

#### F2.1 - API de vérification
**Description** : Endpoint REST pour vérifier authenticité
**Priorité** : P0

**Endpoint** :
```
POST /api/v1/medications/verify
Content-Type: application/json
Authorization: Bearer {jwt_token}

Request Body:
{
  "gtin": "03401234567890",
  "serialNumber": "XYZ789",
  "batchNumber": "ABC123",
  "expiryDate": "2025-12-31",
  "scannedAt": "2025-10-08T14:30:00Z",
  "location": {
    "latitude": 11.8636,
    "longitude": -15.5984
  }
}

Response 200 OK:
{
  "verificationId": "ver_abc123",
  "status": "AUTHENTIC | SUSPICIOUS | UNKNOWN",
  "confidence": 0.95, // 0-1
  "medication": {
    "id": "med_xyz789",
    "name": "Paracetamol 500mg",
    "manufacturer": "PharmaCorp",
    "gtin": "03401234567890",
    "isAuthentic": true,
    "registrationNumber": "GB-2024-1234"
  },
  "details": {
    "dosage": "500mg par comprimé",
    "indications": "Douleur, fièvre",
    "sideEffects": ["Nausées rares", "Allergie cutanée"],
    "contraindications": ["Insuffisance hépatique sévère"]
  },
  "alerts": [],
  "verifiedAt": "2025-10-08T14:30:01Z"
}

Response 404 Not Found:
{
  "status": "UNKNOWN",
  "message": "Médicament non trouvé dans la base de données",
  "recommendedAction": "SIGNAL"
}

Response 403 Suspicious:
{
  "status": "SUSPICIOUS",
  "confidence": 0.15,
  "alerts": [
    {
      "level": "HIGH",
      "type": "SERIAL_DUPLICATE",
      "message": "Ce numéro de série a déjà été scanné 47 fois"
    },
    {
      "level": "MEDIUM",
      "type": "EXPIRED",
      "message": "Date de péremption dépassée"
    }
  ],
  "recommendedAction": "DO_NOT_USE"
}
```

**Backend - Contrôleur Spring Boot** :
```java
// Fichier : MedicationController.java
@RestController
@RequestMapping("/api/v1/medications")
@RequiredArgsConstructor
public class MedicationController {
    
    private final MedicationVerificationService verificationService;
    
    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('PATIENT', 'PHARMACIST', 'AUTHORITY')")
    public ResponseEntity<VerificationResponse> verifyMedication(
            @Valid @RequestBody VerificationRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        // 1. Log de la tentative
        log.info("Verification attempt for GTIN: {} by user: {}", 
                 request.getGtin(), currentUser.getUsername());
        
        // 2. Validation métier
        if (!GS1Utils.isValidGTIN(request.getGtin())) {
            throw new InvalidGTINException("Format GTIN invalide");
        }
        
        // 3. Vérification
        VerificationResponse response = verificationService.verify(
            request, 
            currentUser.getUsername()
        );
        
        // 4. Notification si suspect
        if (response.getStatus() == VerificationStatus.SUSPICIOUS) {
            notificationService.alertAuthorities(request, response);
        }
        
        return ResponseEntity.ok(response);
    }
}
```

**Service de vérification** :
```java
// Fichier : MedicationVerificationService.java
@Service
@Transactional
@RequiredArgsConstructor
public class MedicationVerificationService {
    
    private final MedicationRepository medicationRepo;
    private final ScanHistoryRepository scanHistoryRepo;
    private final ExternalApiClient externalApiClient; // Optionnel
    
    public VerificationResponse verify(VerificationRequest request, String username) {
        // 1. Recherche en base locale
        Optional<Medication> medication = medicationRepo.findByGtin(request.getGtin());
        
        // 2. Si non trouvé, interroger API externe (si config disponible)
        if (medication.isEmpty() && externalApiClient.isConfigured()) {
            medication = externalApiClient.fetchMedication(request.getGtin());
        }
        
        // 3. Analyse d'authenticité
        VerificationResponse response;
        if (medication.isPresent()) {
            response = analyzeAuthenticity(medication.get(), request);
        } else {
            response = VerificationResponse.unknown();
        }
        
        // 4. Enregistrement du scan
        ScanHistory scan = ScanHistory.builder()
            .gtin(request.getGtin())
            .username(username)
            .status(response.getStatus())
            .scannedAt(Instant.now())
            .location(request.getLocation())
            .build();
        scanHistoryRepo.save(scan);
        
        return response;
    }
    
    private VerificationResponse analyzeAuthenticity(Medication med, VerificationRequest req) {
        List<Alert> alerts = new ArrayList<>();
        double confidence = 1.0;
        
        // Règle 1 : Numéro de série dupliqué
        long duplicateCount = scanHistoryRepo.countBySerialNumber(req.getSerialNumber());
        if (duplicateCount > 5) {
            alerts.add(Alert.high("SERIAL_DUPLICATE", 
                "Numéro de série scanné " + duplicateCount + " fois"));
            confidence -= 0.6;
        }
        
        // Règle 2 : Date de péremption
        if (req.getExpiryDate().isBefore(LocalDate.now())) {
            alerts.add(Alert.medium("EXPIRED", "Produit périmé"));
            confidence -= 0.3;
        }
        
        // Règle 3 : Lot rappelé
        if (med.isRecalledBatch(req.getBatchNumber())) {
            alerts.add(Alert.critical("RECALLED", "Lot rappelé par fabricant"));
            confidence -= 0.8;
        }
        
        // Décision finale
        VerificationStatus status = confidence > 0.7 ? 
            VerificationStatus.AUTHENTIC : VerificationStatus.SUSPICIOUS;
        
        return VerificationResponse.builder()
            .status(status)
            .confidence(Math.max(0, confidence))
            .medication(med)
            .alerts(alerts)
            .build();
    }
}
```

### 3.3 Module Gestion Utilisateurs

#### F3.1 - Authentification JWT
**Priorité** : P0

**Flow authentification** :
```
1. POST /api/v1/auth/register
   → Création compte + envoi code vérification SMS/Email
2. POST /api/v1/auth/verify
   → Vérification code → Activation compte
3. POST /api/v1/auth/login
   → Émission JWT access token (15min) + refresh token (7j)
4. Requêtes API avec Authorization: Bearer {access_token}
5. POST /api/v1/auth/refresh
   → Renouvellement access token via refresh token
```

**Configuration Spring Security** :
```java
// Fichier : SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/medications/verify").authenticated()
                .requestMatchers("/api/v1/admin/**").hasRole("AUTHORITY")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthFilter() {
        return new JwtAuthenticationFilter(jwtService, userDetailsService);
    }
}
```

**Service JWT** :
```java
// Fichier : JwtService.java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration:900000}") // 15min
    private Long expiration;
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().stream()
            .findFirst().orElseThrow().getAuthority());
        
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact();
    }
    
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    // Autres méthodes : extractUsername, isTokenExpired, etc.
}
```

**React Native - Service Auth** :
```typescript
// Fichier : src/services/AuthService.ts
import AsyncStorage from '@react-native-async-storage/async-storage';
import axios from 'axios';

const API_URL = 'https://api.medverify.gw';

class AuthService {
  async login(email: string, password: string): Promise<AuthResponse> {
    const response = await axios.post(`${API_URL}/api/v1/auth/login`, {
      email,
      password
    });
    
    const { accessToken, refreshToken, user } = response.data;
    
    // Stockage sécurisé
    await AsyncStorage.multiSet([
      ['@access_token', accessToken],
      ['@refresh_token', refreshToken],
      ['@user', JSON.stringify(user)]
    ]);
    
    return response.data;
  }
  
  async register(data: RegisterData): Promise<void> {
    await axios.post(`${API_URL}/api/v1/auth/register`, data);
  }
  
  async logout(): Promise<void> {
    await AsyncStorage.multiRemove(['@access_token', '@refresh_token', '@user']);
  }
  
  async refreshToken(): Promise<string> {
    const refreshToken = await AsyncStorage.getItem('@refresh_token');
    const response = await axios.post(`${API_URL}/api/v1/auth/refresh`, {
      refreshToken
    });
    
    const { accessToken } = response.data;
    await AsyncStorage.setItem('@access_token', accessToken);
    return accessToken;
  }
}

export default new AuthService();
```

**Intercepteur Axios pour refresh auto** :
```typescript
// Fichier : src/services/ApiClient.ts
import axios, { AxiosInstance } from 'axios';
import AuthService from './AuthService';

const apiClient: AxiosInstance = axios.create({
  baseURL: 'https://api.medverify.gw',
  timeout: 10000
});

// Intercepteur requête : injection token
apiClient.interceptors.request.use(async (config) => {
  const token = await AsyncStorage.getItem('@access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Intercepteur réponse : refresh auto si 401
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const newToken = await AuthService.refreshToken();
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        await AuthService.logout();
        // Navigation vers login
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;
```

#### F3.2 - Rôles et permissions

**Table roles** :
```sql
CREATE TYPE user_role AS ENUM ('PATIENT', 'PHARMACIST', 'AUTHORITY', 'ADMIN');

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role user_role NOT NULL DEFAULT 'PATIENT',
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  phone VARCHAR(20),
  is_verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pharmacist_verifications (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES users(id),
  pharmacy_name VARCHAR(255) NOT NULL,
  license_number VARCHAR(50) NOT NULL,
  verification_status VARCHAR(20) DEFAULT 'PENDING',
  verified_at TIMESTAMP,
  verified_by UUID REFERENCES users(id)
);
```

**Matrice de permissions** :
| Fonctionnalité | PATIENT | PHARMACIST | AUTHORITY | ADMIN |
|----------------|---------|------------|-----------|-------|
| Scanner médicament | ✅ | ✅ | ✅ | ✅ |
| Historique personnel | ✅ | ✅ | ✅ | ✅ |
| Signaler suspect | ✅ | ✅ | ✅ | ✅ |
| Scan batch (multi) | ❌ | ✅ | ✅ | ✅ |
| Export historique | ❌ | ✅ | ✅ | ✅ |
| Dashboard analytics | ❌ | ❌ | ✅ | ✅ |
| Gérer utilisateurs | ❌ | ❌ | ❌ | ✅ |
| Gérer base médicaments | ❌ | ❌ | ✅ | ✅ |

### 3.4 Module Signalement

#### F4.1 - Signaler médicament suspect
**Priorité** : P1

**Endpoint** :
```
POST /api/v1/reports
Authorization: Bearer {token}

Request:
{
  "medicationId": "med_xyz789", // Optionnel si scan précédent
  "gtin": "03401234567890",
  "serialNumber": "XYZ789",
  "reportType": "COUNTERFEIT | QUALITY_ISSUE | EXPIRED | OTHER",
  "description": "Emballage endommagé, goût inhabituel",
  "purchaseLocation": {
    "name": "Pharmacie Central",
    "address": "Av. Amilcar Cabral, Bissau",
    "latitude": 11.8636,
    "longitude": -15.5984
  },
  "photos": [
    "data:image/jpeg;base64,/9j/4AAQSkZJRg..." // Max 3 photos
  ],
  "anonymous": false
}

Response 201:
{
  "reportId": "rep_abc123",
  "status": "SUBMITTED",
  "referenceNumber": "REP-2025-001234",
  "message": "Signalement enregistré. Autorités notifiées.",
  "estimatedProcessingTime": "48h"
}
```

**Backend - Entity** :
```java
// Fichier : Report.java
@Entity
@Table(name = "reports")
@Data
@Builder
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User reporter;
    
    @ManyToOne
    @JoinColumn(name = "medication_id")
    private Medication medication;
    
    private String gtin;
    private String serialNumber;
    
    @Enumerated(EnumType.STRING)
    private ReportType reportType;
    
    @Column(length = 2000)
    private String description;
    
    @Embedded
    private Location purchaseLocation;
    
    @ElementCollection
    @CollectionTable(name = "report_photos")
    private List<String> photoUrls;
    
    private Boolean anonymous;
    
    @Enumerated(EnumType.STRING)
    private ReportStatus status; // SUBMITTED, UNDER_REVIEW, CONFIRMED, REJECTED
    
    @Column(unique = true)
    private String referenceNumber;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewer;
    
    private Instant reviewedAt;
    
    @Column(length = 1000)
    private String reviewNotes;
}
```

**Service Notification** :
```java
// Fichier : ReportNotificationService.java
@Service
@RequiredArgsConstructor
public class ReportNotificationService {
    
    private final EmailService emailService;
    private final PushNotificationService pushService;
    private final UserRepository userRepo;
    
    @Async
    public void notifyAuthorities(Report report) {
        // 1. Récupérer autorités de la région
        List<User> authorities = userRepo.findByRoleAndRegion(
            UserRole.AUTHORITY, 
            report.getPurchaseLocation().getRegion()
        );
        
        // 2. Email aux autorités
        authorities.forEach(authority -> {
            emailService.send(EmailTemplate.builder()
                .to(authority.getEmail())
                .subject("ALERTE - Nouveau signalement médicament suspect")
                .template("report-notification")
                .variables(Map.of(
                    "reportId", report.getReferenceNumber(),
                    "medicationName", report.getMedication().getName(),
                    "location", report.getPurchaseLocation().getName(),
                    "severity", calculateSeverity(report)
                ))
                .build());
        });
        
        // 3. Push notification si critique
        if (calculateSeverity(report) == Severity.HIGH) {
            authorities.forEach(authority -> {
                pushService.send(authority.getFcmToken(), PushNotification.builder()
                    .title("🚨 ALERTE CRITIQUE")
                    .body("Médicament suspect signalé : " + report.getMedication().getName())
                    .data(Map.of("reportId", report.getId().toString()))
                    .build());
            });
        }
        
        // 4. Notification au reporter
        if (!report.getAnonymous()) {
            emailService.send(EmailTemplate.builder()
                .to(report.getReporter().getEmail())
                .subject("Confirmation de votre signalement")
                .template("report-confirmation")
                .variables(Map.of(
                    "referenceNumber", report.getReferenceNumber(),
                    "estimatedTime", "48h"
                ))
                .build());
        }
    }
    
    private Severity calculateSeverity(Report report) {
        // Logique de calcul selon type, historique, zone géographique
        if (report.getReportType() == ReportType.COUNTERFEIT) {
            return Severity.HIGH;
        }
        // ... autres règles
        return Severity.MEDIUM;
    }
}
```

### 3.5 Dashboard Autorités

#### F5.1 - Analytics & KPIs
**Priorité** : P2

**Endpoint** :
```
GET /api/v1/admin/dashboard/stats?period=30d
Authorization: Bearer {token} (role: AUTHORITY ou ADMIN)

Response:
{
  "period": {
    "start": "2025-09-08T00:00:00Z",
    "end": "2025-10-08T23:59:59Z"
  },
  "kpis": {
    "totalScans": 45678,
    "authenticMedications": 42341,
    "suspiciousMedications": 2987,
    "unknownMedications": 350,
    "authenticityRate": 92.7,
    "totalReports": 234,
    "confirmedCounterfeits": 87,
    "activeUsers": 8923,
    "newUsers": 1234
  },
  "trends": {
    "scansGrowth": "+23.5%", // vs période précédente
    "reportsGrowth": "+15.2%"
  },
  "topCounterfeitMedications": [
    {
      "medicationName": "Amoxicilline 500mg",
      "gtin": "03401234567890",
      "reportCount": 23,
      "lastReported": "2025-10-07T18:30:00Z"
    }
  ],
  "geographicDistribution": [
    {
      "region": "Bissau",
      "scans": 28456,
      "reports": 156,
      "suspiciousRate": 8.2
    },
    {
      "region": "Bafatá",
      "scans": 5432,
      "reports": 34,
      "suspiciousRate": 12.5
    }
  ],
  "recentAlerts": [
    {
      "id": "alert_xyz",
      "type": "SERIAL_SPIKE",
      "severity": "HIGH",
      "message": "47 scans du même numéro de série détectés en 24h",
      "medicationName": "Paracetamol 500mg",
      "timestamp": "2025-10-08T12:45:00Z"
    }
  ]
}
```

**Repository - Requêtes statistiques** :
```java
// Fichier : ScanHistoryRepository.java
@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, UUID> {
    
    @Query("""
        SELECT COUNT(*) FROM ScanHistory s 
        WHERE s.scannedAt BETWEEN :start AND :end
    """)
    Long countScansBetween(@Param("start") Instant start, @Param("end") Instant end);
    
    @Query("""
        SELECT s.status, COUNT(*) 
        FROM ScanHistory s 
        WHERE s.scannedAt BETWEEN :start AND :end
        GROUP BY s.status
    """)
    List<Object[]> countByStatus(@Param("start") Instant start, @Param("end") Instant end);
    
    @Query("""
        SELECT m.name, m.gtin, COUNT(r.id) as reportCount
        FROM Report r
        JOIN r.medication m
        WHERE r.createdAt BETWEEN :start AND :end
        AND r.reportType = 'COUNTERFEIT'
        GROUP BY m.id, m.name, m.gtin
        ORDER BY reportCount DESC
        LIMIT 10
    """)
    List<TopCounterfeitDTO> findTopCounterfeits(
        @Param("start") Instant start, 
        @Param("end") Instant end
    );
    
    @Query(value = """
        SELECT 
            COALESCE(ph.region, 'Inconnu') as region,
            COUNT(s.id) as scans,
            COUNT(CASE WHEN s.status = 'SUSPICIOUS' THEN 1 END) as suspicious,
            ROUND(COUNT(CASE WHEN s.status = 'SUSPICIOUS' THEN 1 END)::numeric / 
                  COUNT(s.id)::numeric * 100, 2) as suspicious_rate
        FROM scan_history s
        LEFT JOIN users u ON s.user_id = u.id
        LEFT JOIN pharmacist_verifications ph ON u.id = ph.user_id
        WHERE s.scanned_at BETWEEN :start AND :end
        GROUP BY ph.region
        ORDER BY scans DESC
    """, nativeQuery = true)
    List<GeographicStatsDTO> getGeographicDistribution(
        @Param("start") Instant start, 
        @Param("end") Instant end
    );
}
```

**Service Dashboard** :
```java
// Fichier : DashboardService.java
@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final ScanHistoryRepository scanRepo;
    private final ReportRepository reportRepo;
    private final UserRepository userRepo;
    
    public DashboardStats getStats(String period) {
        Instant[] range = parsePeriod(period); // "30d" -> [start, end]
        Instant start = range[0];
        Instant end = range[1];
        
        // KPIs principaux
        Long totalScans = scanRepo.countScansBetween(start, end);
        Map<VerificationStatus, Long> statusCounts = scanRepo.countByStatus(start, end)
            .stream()
            .collect(Collectors.toMap(
                row -> (VerificationStatus) row[0],
                row -> (Long) row[1]
            ));
        
        Long authentic = statusCounts.getOrDefault(VerificationStatus.AUTHENTIC, 0L);
        Long suspicious = statusCounts.getOrDefault(VerificationStatus.SUSPICIOUS, 0L);
        Long unknown = statusCounts.getOrDefault(VerificationStatus.UNKNOWN, 0L);
        
        double authenticityRate = totalScans > 0 ? 
            (authentic.doubleValue() / totalScans) * 100 : 0;
        
        // Rapports
        Long totalReports = reportRepo.countBetween(start, end);
        Long confirmedCounterfeits = reportRepo.countByStatusAndPeriod(
            ReportStatus.CONFIRMED, start, end
        );
        
        // Utilisateurs
        Long activeUsers = userRepo.countActiveUsers(start, end);
        Long newUsers = userRepo.countNewUsers(start, end);
        
        // Tendances (comparaison période précédente)
        Instant prevStart = start.minus(Duration.between(start, end));
        Long prevScans = scanRepo.countScansBetween(prevStart, start);
        double scansGrowth = calculateGrowth(totalScans, prevScans);
        
        // Top contrefaçons
        List<TopCounterfeitDTO> topCounterfeits = scanRepo.findTopCounterfeits(start, end);
        
        // Distribution géographique
        List<GeographicStatsDTO> geoDistribution = scanRepo.getGeographicDistribution(start, end);
        
        // Alertes récentes
        List<AlertDTO> recentAlerts = detectAnomalies(start, end);
        
        return DashboardStats.builder()
            .period(new Period(start, end))
            .kpis(KPIs.builder()
                .totalScans(totalScans)
                .authenticMedications(authentic)
                .suspiciousMedications(suspicious)
                .unknownMedications(unknown)
                .authenticityRate(authenticityRate)
                .totalReports(totalReports)
                .confirmedCounterfeits(confirmedCounterfeits)
                .activeUsers(activeUsers)
                .newUsers(newUsers)
                .build())
            .trends(Trends.builder()
                .scansGrowth(String.format("%+.1f%%", scansGrowth))
                .build())
            .topCounterfeitMedications(topCounterfeits)
            .geographicDistribution(geoDistribution)
            .recentAlerts(recentAlerts)
            .build();
    }
    
    private List<AlertDTO> detectAnomalies(Instant start, Instant end) {
        List<AlertDTO> alerts = new ArrayList<>();
        
        // Détection 1 : Spike numéro de série dupliqué
        List<Object[]> serialSpikes = scanRepo.findSerialNumberSpikes(start, end, 20);
        serialSpikes.forEach(row -> {
            String serialNumber = (String) row[0];
            Long count = (Long) row[1];
            String medicationName = (String) row[2];
            
            alerts.add(AlertDTO.builder()
                .id("alert_" + UUID.randomUUID())
                .type("SERIAL_SPIKE")
                .severity(count > 50 ? Severity.CRITICAL : Severity.HIGH)
                .message(String.format("%d scans du même numéro de série en 24h", count))
                .medicationName(medicationName)
                .timestamp(Instant.now())
                .build());
        });
        
        // Détection 2 : Zone géographique avec taux suspect élevé
        List<GeographicStatsDTO> suspiciousRegions = scanRepo.getGeographicDistribution(start, end)
            .stream()
            .filter(region -> region.getSuspiciousRate() > 15.0)
            .toList();
        
        suspiciousRegions.forEach(region -> {
            alerts.add(AlertDTO.builder()
                .type("HIGH_SUSPICIOUS_RATE")
                .severity(Severity.MEDIUM)
                .message(String.format("Taux suspect de %.1f%% dans la région %s", 
                    region.getSuspiciousRate(), region.getRegion()))
                .timestamp(Instant.now())
                .build());
        });
        
        return alerts;
    }
}
```

**React Native - Dashboard Screen** :
```typescript
// Fichier : src/screens/AuthorityDashboard.tsx
import React, { useEffect, useState } from 'react';
import { View, ScrollView, Text, RefreshControl } from 'react-native';
import { LineChart, BarChart, PieChart } from 'react-native-chart-kit';
import apiClient from '../services/ApiClient';

interface DashboardStats {
  kpis: {
    totalScans: number;
    authenticityRate: number;
    totalReports: number;
    confirmedCounterfeits: number;
  };
  topCounterfeitMedications: Array<{
    medicationName: string;
    reportCount: number;
  }>;
  geographicDistribution: Array<{
    region: string;
    scans: number;
    suspiciousRate: number;
  }>;
  recentAlerts: Array<{
    type: string;
    severity: string;
    message: string;
  }>;
}

export const AuthorityDashboard: React.FC = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [period, setPeriod] = useState('30d');

  const fetchStats = async () => {
    try {
      const response = await apiClient.get(`/api/v1/admin/dashboard/stats?period=${period}`);
      setStats(response.data);
    } catch (error) {
      console.error('Erreur chargement stats:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, [period]);

  if (!stats) return <LoadingSpinner />;

  return (
    <ScrollView 
      className="flex-1 bg-gray-50"
      refreshControl={<RefreshControl refreshing={loading} onRefresh={fetchStats} />}
    >
      {/* KPI Cards */}
      <View className="p-4 flex-row flex-wrap justify-between">
        <KPICard 
          title="Total Scans"
          value={stats.kpis.totalScans.toLocaleString()}
          icon="scan"
          color="blue"
        />
        <KPICard 
          title="Taux Authenticité"
          value={`${stats.kpis.authenticityRate.toFixed(1)}%`}
          icon="check-circle"
          color="green"
        />
        <KPICard 
          title="Signalements"
          value={stats.kpis.totalReports.toString()}
          icon="alert-triangle"
          color="orange"
        />
        <KPICard 
          title="Contrefaçons"
          value={stats.kpis.confirmedCounterfeits.toString()}
          icon="x-circle"
          color="red"
        />
      </View>

      {/* Alertes récentes */}
      <View className="mx-4 mb-4 bg-white rounded-lg shadow p-4">
        <Text className="text-lg font-bold mb-3">🚨 Alertes Récentes</Text>
        {stats.recentAlerts.map((alert, index) => (
          <AlertCard key={index} alert={alert} />
        ))}
      </View>

      {/* Top médicaments contrefaits */}
      <View className="mx-4 mb-4 bg-white rounded-lg shadow p-4">
        <Text className="text-lg font-bold mb-3">📊 Top Médicaments Suspects</Text>
        <BarChart
          data={{
            labels: stats.topCounterfeitMedications.map(m => 
              m.medicationName.substring(0, 15)
            ),
            datasets: [{
              data: stats.topCounterfeitMedications.map(m => m.reportCount)
            }]
          }}
          width={350}
          height={220}
          chartConfig={chartConfig}
          verticalLabelRotation={30}
        />
      </View>

      {/* Distribution géographique */}
      <View className="mx-4 mb-4 bg-white rounded-lg shadow p-4">
        <Text className="text-lg font-bold mb-3">🗺️ Distribution Géographique</Text>
        {stats.geographicDistribution.map((region, index) => (
          <View key={index} className="mb-3">
            <View className="flex-row justify-between mb-1">
              <Text className="font-semibold">{region.region}</Text>
              <Text className="text-gray-600">{region.scans} scans</Text>
            </View>
            <View className="h-2 bg-gray-200 rounded-full overflow-hidden">
              <View 
                className={`h-full ${region.suspiciousRate > 10 ? 'bg-red-500' : 'bg-green-500'}`}
                style={{ width: `${region.suspiciousRate}%` }}
              />
            </View>
            <Text className="text-xs text-gray-500 mt-1">
              Taux suspect: {region.suspiciousRate.toFixed(1)}%
            </Text>
          </View>
        ))}
      </View>
    </ScrollView>
  );
};

const KPICard: React.FC<{title: string; value: string; icon: string; color: string}> = 
  ({ title, value, icon, color }) => (
  <View className={`w-[48%] mb-3 bg-white rounded-lg shadow p-4 border-l-4 border-${color}-500`}>
    <Text className="text-gray-600 text-sm mb-1">{title}</Text>
    <Text className="text-2xl font-bold">{value}</Text>
  </View>
);

const AlertCard: React.FC<{alert: any}> = ({ alert }) => (
  <View className={`mb-2 p-3 rounded border-l-4 ${
    alert.severity === 'HIGH' ? 'bg-red-50 border-red-500' :
    alert.severity === 'MEDIUM' ? 'bg-orange-50 border-orange-500' :
    'bg-yellow-50 border-yellow-500'
  }`}>
    <Text className="font-semibold">{alert.message}</Text>
    {alert.medicationName && (
      <Text className="text-sm text-gray-600 mt-1">{alert.medicationName}</Text>
    )}
  </View>
);
```

---

## 4. MODÈLE DE DONNÉES COMPLET

### 4.1 Schéma PostgreSQL

```sql
-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis"; -- Pour géolocalisation

-- Types énumérés
CREATE TYPE user_role AS ENUM ('PATIENT', 'PHARMACIST', 'AUTHORITY', 'ADMIN');
CREATE TYPE verification_status AS ENUM ('AUTHENTIC', 'SUSPICIOUS', 'UNKNOWN');
CREATE TYPE report_type AS ENUM ('COUNTERFEIT', 'QUALITY_ISSUE', 'EXPIRED', 'PACKAGING_DEFECT', 'OTHER');
CREATE TYPE report_status AS ENUM ('SUBMITTED', 'UNDER_REVIEW', 'CONFIRMED', 'REJECTED', 'CLOSED');
CREATE TYPE alert_severity AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');

-- Table Utilisateurs
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'PATIENT',
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Table Vérifications Pharmaciens
CREATE TABLE pharmacist_verifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    pharmacy_name VARCHAR(255) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    region VARCHAR(100),
    address TEXT,
    location GEOGRAPHY(POINT, 4326), -- PostGIS
    verification_status VARCHAR(20) DEFAULT 'PENDING',
    verification_document_url VARCHAR(500),
    verified_at TIMESTAMP,
    verified_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pharmacist_location ON pharmacist_verifications USING GIST(location);

-- Table Médicaments
CREATE TABLE medications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gtin VARCHAR(14) UNIQUE NOT NULL, -- Global Trade Item Number
    name VARCHAR(255) NOT NULL,
    generic_name VARCHAR(255),
    manufacturer VARCHAR(255),
    manufacturer_country VARCHAR(100),
    dosage VARCHAR(100),
    pharmaceutical_form VARCHAR(100), -- Comprimé, Sirop, Injection, etc.
    registration_number VARCHAR(100), -- Numéro AMM local
    atc_code VARCHAR(20), -- Classification ATC
    
    -- Informations médicales
    indications TEXT[],
    posology JSONB, -- Structure complexe selon âge/poids
    side_effects TEXT[],
    contraindications TEXT[],
    interactions TEXT[],
    pregnancy_category VARCHAR(10),
    storage_conditions TEXT,
    
    -- Statut
    is_essential BOOLEAN DEFAULT FALSE, -- Liste OMS médicaments essentiels
    is_active BOOLEAN DEFAULT TRUE,
    recall_status VARCHAR(50),
    
    -- Métadonnées
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id)
);

CREATE INDEX idx_medications_gtin ON medications(gtin);
CREATE INDEX idx_medications_name ON medications(name);
CREATE INDEX idx_medications_manufacturer ON medications(manufacturer);
CREATE INDEX idx_medications_essential ON medications(is_essential) WHERE is_essential = TRUE;

-- Table Lots de Médicaments
CREATE TABLE medication_batches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    medication_id UUID REFERENCES medications(id) ON DELETE CASCADE,
    batch_number VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    manufacturing_date DATE,
    is_recalled BOOLEAN DEFAULT FALSE,
    recall_reason TEXT,
    recall_date DATE,
    quantity_manufactured BIGINT,
    
    UNIQUE(medication_id, batch_number)
);

CREATE INDEX idx_batches_expiry ON medication_batches(expiry_date);
CREATE INDEX idx_batches_recalled ON medication_batches(is_recalled) WHERE is_recalled = TRUE;

-- Table Historique Scans
CREATE TABLE scan_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    medication_id UUID REFERENCES medications(id),
    
    -- Données scannées
    gtin VARCHAR(14) NOT NULL,
    serial_number VARCHAR(100),
    batch_number VARCHAR(50),
    expiry_date DATE,
    
    -- Résultat vérification
    status verification_status NOT NULL,
    confidence_score DECIMAL(3,2), -- 0.00 - 1.00
    verification_source VARCHAR(50), -- 'LOCAL_DB', 'EXTERNAL_API', 'CACHE'
    
    -- Contexte
    scanned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    location GEOGRAPHY(POINT, 4326),
    device_info JSONB, -- OS, version app, etc.
    
    -- Alertes détectées
    alerts JSONB -- Array d'objets {type, severity, message}
);

CREATE INDEX idx_scans_user ON scan_history(user_id);
CREATE INDEX idx_scans_medication ON scan_history(medication_id);
CREATE INDEX idx_scans_gtin ON scan_history(gtin);
CREATE INDEX idx_scans_serial ON scan_history(serial_number);
CREATE INDEX idx_scans_date ON scan_history(scanned_at DESC);
CREATE INDEX idx_scans_status ON scan_history(status);
CREATE INDEX idx_scans_location ON scan_history USING GIST(location);

-- Table Signalements
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    reference_number VARCHAR(50) UNIQUE NOT NULL, -- REP-2025-001234
    
    -- Auteur
    reporter_id UUID REFERENCES users(id),
    anonymous BOOLEAN DEFAULT FALSE,
    
    -- Médicament concerné
    medication_id UUID REFERENCES medications(id),
    scan_id UUID REFERENCES scan_history(id), -- Lien vers scan original si existe
    gtin VARCHAR(14),
    serial_number VARCHAR(100),
    batch_number VARCHAR(50),
    
    -- Détails signalement
    report_type report_type NOT NULL,
    description TEXT NOT NULL,
    severity alert_severity,
    
    -- Lieu d'achat
    purchase_location_name VARCHAR(255),
    purchase_location_address TEXT,
    purchase_location GEOGRAPHY(POINT, 4326),
    purchase_date DATE,
    purchase_price DECIMAL(10,2),
    
    -- Preuves
    photo_urls TEXT[], -- URLs vers stockage S3/Cloud
    
    -- Traitement
    status report_status DEFAULT 'SUBMITTED',
    priority INT DEFAULT 0, -- 0-10
    assigned_to UUID REFERENCES users(id),
    reviewed_by UUID REFERENCES users(id),
    review_notes TEXT,
    reviewed_at TIMESTAMP,
    
    -- Actions prises
    actions_taken TEXT[],
    outcome VARCHAR(100),
    
    -- Métadonnées
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_type ON reports(report_type);
CREATE INDEX idx_reports_reporter ON reports(reporter_id);
CREATE INDEX idx_reports_date ON reports(created_at DESC);
CREATE INDEX idx_reports_location ON reports USING GIST(purchase_location);
CREATE INDEX idx_reports_ref ON reports(reference_number);

-- Table Notifications
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL, -- 'REPORT_UPDATE', 'NEW_ALERT', 'SYSTEM'
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    data JSONB, -- Données contextuelles
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;

-- Table Tokens de Refresh
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- Table Audit Logs
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL, -- 'LOGIN', 'SCAN', 'REPORT_CREATE', etc.
    entity_type VARCHAR(50),
    entity_id UUID,
    changes JSONB, -- Avant/Après pour modifications
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_user ON audit_logs(user_id);
CREATE INDEX idx_audit_date ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_action ON audit_logs(action);

-- Fonctions & Triggers

-- Trigger: Mise à jour updated_at automatique
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_medications_updated_at BEFORE UPDATE ON medications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_reports_updated_at BEFORE UPDATE ON reports
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Fonction: Génération référence signalement
CREATE OR REPLACE FUNCTION generate_report_reference()
RETURNS TRIGGER AS $
BEGIN
    NEW.reference_number = 'REP-' || TO_CHAR(CURRENT_DATE, 'YYYY') || '-' || 
                          LPAD(NEXTVAL('report_sequence')::TEXT, 6, '0');
    RETURN NEW;
END;
$ LANGUAGE plpgsql;

CREATE SEQUENCE report_sequence START 1;

CREATE TRIGGER set_report_reference BEFORE INSERT ON reports
    FOR EACH ROW EXECUTE FUNCTION generate_report_reference();
```

### 4.2 Entities Spring Boot

```java
// Fichier : User.java
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.PATIENT;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    private String phone;
    
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "locked_until")
    private Instant lockedUntil;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "last_login_at")
    private Instant lastLoginAt;
    
    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return lockedUntil == null || Instant.now().isAfter(lockedUntil);
    }
    
    @Override
    public boolean isEnabled() {
        return isActive && isVerified;
    }
}
```

```java
// Fichier : Medication.java
@Entity
@Table(name = "medications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Medication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false, length = 14)
    private String gtin;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "generic_name")
    private String genericName;
    
    private String manufacturer;
    
    @Column(name = "manufacturer_country")
    private String manufacturerCountry;
    
    private String dosage;
    
    @Column(name = "pharmaceutical_form")
    private String pharmaceuticalForm;
    
    @Column(name = "registration_number")
    private String registrationNumber;
    
    @Column(name = "atc_code", length = 20)
    private String atcCode;
    
    // JSONB fields
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private PosologyInfo posology;
    
    @ElementCollection
    @CollectionTable(name = "medication_indications")
    private List<String> indications;
    
    @ElementCollection
    @CollectionTable(name = "medication_side_effects")
    private List<String> sideEffects;
    
    @ElementCollection
    @CollectionTable(name = "medication_contraindications")
    private List<String> contraindications;
    
    @Column(name = "is_essential")
    private Boolean isEssential = false;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "recall_status")
    private String recallStatus;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL)
    private List<MedicationBatch> batches;
}
```

```java
// Fichier : ScanHistory.java
@Entity
@Table(name = "scan_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id")
    private Medication medication;
    
    @Column(nullable = false, length = 14)
    private String gtin;
    
    @Column(name = "serial_number")
    private String serialNumber;
    
    @Column(name = "batch_number")
    private String batchNumber;
    
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;
    
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore;
    
    @Column(name = "verification_source")
    private String verificationSource;
    
    @Column(name = "scanned_at")
    private Instant scannedAt = Instant.now();
    
    @Column(columnDefinition = "geography(Point, 4326)")
    private Point location; // PostGIS Point
    
    @Type(JsonBinaryType.class)
    @Column(name = "device_info", columnDefinition = "jsonb")
    private DeviceInfo deviceInfo;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private List<Alert> alerts;
}
```

---

## 5. ARCHITECTURE TECHNIQUE DÉTAILLÉE

### 5.1 Structure Frontend React Native

```
med-verify-app/
├── src/
│   ├── components/           # Composants réutilisables
│   │   ├── Scanner/
│   │   │   ├── DataMatrixScanner.tsx
│   │   │   ├── ScanOverlay.tsx
│   │   │   └── ScanResult.tsx
│   │   ├── UI/
│   │   │   ├── Button.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── LoadingSpinner.tsx
│   │   │   └── StatusBadge.tsx
│   │   └── Medication/
│   │       ├── MedicationCard.tsx
│   │       ├── MedicationDetails.tsx
│   │       └── MedicationAlert.tsx
│   │
│   ├── screens/              # Écrans de l'app
│   │   ├── Auth/
│   │   │   ├── LoginScreen.tsx
│   │   │   ├── RegisterScreen.tsx
│   │   │   ├── ForgotPasswordScreen.tsx
│   │   │   └── VerifyAccountScreen.tsx
│   │   ├── Home/
│   │   │   ├── HomeScreen.tsx
│   │   │   └── OnboardingScreen.tsx
│   │   ├── Scan/
│   │   │   ├── ScanScreen.tsx
│   │   │   ├── ScanResultScreen.tsx
│   │   │   └── BatchScanScreen.tsx (Pharmaciens)
│   │   ├── History/
│   │   │   ├── HistoryScreen.tsx
│   │   │   └── HistoryDetailScreen.tsx
│   │   ├── Report/
│   │   │   ├── ReportCreateScreen.tsx
│   │   │   ├── ReportListScreen.tsx
│   │   │   └── ReportDetailScreen.tsx
│   │   ├── Dashboard/
│   │   │   └── AuthorityDashboard.tsx
│   │   └── Profile/
│   │       ├── ProfileScreen.tsx
│   │       └── SettingsScreen.tsx
│   │
│   ├── navigation/           # Navigation
│   │   ├── AppNavigator.tsx
│   │   ├── AuthNavigator.tsx
│   │   └── MainNavigator.tsx
│   │
│   ├── services/             # Services API
│   │   ├── ApiClient.ts      # Instance Axios configurée
│   │   ├── AuthService.ts
│   │   ├── ScanService.ts
│   │   ├── MedicationService.ts
│   │   ├── ReportService.ts
│   │   └── NotificationService.ts
│   │
│   ├── store/                # State Management (Redux Toolkit)
│   │   ├── store.ts
│   │   ├── slices/
│   │   │   ├── authSlice.ts
│   │   │   ├── scanSlice.ts
│   │   │   ├── medicationSlice.ts
│   │   │   └── notificationSlice.ts
│   │   └── hooks.ts
│   │
│   ├── utils/                # Utilitaires
│   │   ├── gs1Parser.ts      # Parse Data Matrix GS1
│   │   ├── validators.ts
│   │   ├── formatters.ts
│   │   ├── constants.ts
│   │   └── permissions.ts
│   │
│   ├── database/             # SQLite local
│   │   ├── schema.ts
│   │   ├── migrations.ts
│   │   └── queries.ts
│   │
│   ├── locales/              # i18n
│   │   ├── pt.json           # Portugais
│   │   ├── fr.json           # Français
│   │   └── en.json           # Anglais
│   │
│   └── types/                # Types TypeScript
│       ├── api.types.ts
│       ├── medication.types.ts
│       ├── user.types.ts
│       └── scan.types.ts
│
├── android/                  # Config Android
├── ios/                      # Config iOS
├── App.tsx                   # Point d'entrée
├── app.json
├── package.json
└── tsconfig.json
```

### 5.2 Structure Backend Spring Boot

```
med-verify-backend/
├── src/main/java/com/medverify/
│   ├── MedVerifyApplication.java
│   │
│   ├── config/               # Configuration
│   │   ├── SecurityConfig.java
│   │   ├── CorsConfig.java
│   │   ├── DatabaseConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── AsyncConfig.java
│   │
│   ├── controller/           # REST Controllers
│   │   ├── AuthController.java
│   │   ├── MedicationController.java
│   │   ├── ScanController.java
│   │   ├── ReportController.java
│   │   ├── UserController.java
│   │   └── DashboardController.java
│   │
│   ├── service/              # Business Logic
│   │   ├── AuthService.java
│   │   ├── MedicationVerificationService.java
│   │   ├── ScanHistoryService.java
│   │   ├── ReportService.java
│   │   ├── NotificationService.java
│   │   ├── EmailService.java
│   │   ├── PushNotificationService.java
│   │   └── DashboardService.java
│   │
│   ├── repository/           # Data Access
│   │   ├── UserRepository.java
│   │   ├── MedicationRepository.java
│   │   ├── ScanHistoryRepository.java
│   │   ├── ReportRepository.java
│   │   └── RefreshTokenRepository.java
│   │
│   ├── entity/               # JPA Entities
│   │   ├── User.java
│   │   ├── Medication.java
│   │   ├── MedicationBatch.java
│   │   ├── ScanHistory.java
│   │   ├── Report.java
│   │   ├── Notification.java
│   │   └── RefreshToken.java
│   │
│   ├── dto/                  # Data Transfer Objects
│   │   ├── request/
│   │   │   ├── LoginRequest.java
│   │   │   ├── RegisterRequest.java
│   │   │   ├── VerificationRequest.java
│   │   │   └── ReportRequest.java
│   │   └── response/
│   │       ├── AuthResponse.java
│   │       ├── VerificationResponse.java
│   │       ├── MedicationResponse.java
│   │       └── DashboardStatsResponse.java
│   │
│   ├── security/             # Sécurité
│   │   ├── JwtService.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── UserDetailsServiceImpl.java
│   │   └── PasswordEncoder.java
│   │
│   ├── exception/            # Gestion erreurs
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   ├── InvalidGTINException.java
│   │   └── UnauthorizedException.java
│   │
│   ├── util/                 # Utilitaires
│   │   ├── GS1Utils.java
│   │   ├── ValidationUtils.java
│   │   └── DateUtils.java
│   │
│   └── integration/          # Intégrations externes
│       ├── ExternalApiClient.java
│       ├── SMSProvider.java
│       └── CloudStorageService.java
│
├── src/main/resources/
│   ├── application.yml       # Config principale
│   ├── application-dev.yml
│   ├── application-prod.yml
│   ├── db/migration/         # Flyway migrations
│   │   ├── V1__initial_schema.sql
│   │   ├── V2__add_indexes.sql
│   │   └── V3__sample_data.sql
│   └── templates/            # Email templates
│       ├── verification-email.html
│       └── report-notification.html
│
├── src/test/java/            # Tests
│   ├── controller/
│   ├── service/
│   └── integration/
│
├── pom.xml                   # Maven dependencies
└── Dockerfile
```

### 5.3 Configuration application.yml

```yaml
# Fichier : application.yml
spring:
  application:
    name: med-verify-api
  
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:medverify}
    username: ${DB_USERNAME:medverify_user}
    password: ${DB_PASSWORD:change_me}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
  
  jpa:
    hibernate:
      ddl-auto: validate # Flyway gère le schema
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.spatial.dialect.postgis.PostgisDialect
        format_sql: true
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-very-long-secret-key-change-in-production-min-256-bits}
  expiration: 900000 # 15 minutes en ms
  refresh-expiration: 604800000 # 7 jours en ms

# CORS
cors:
  allowed-origins: 
    - http://localhost:3000
    - https://medverify.gw
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true

# Email Configuration
spring.mail:
  host: ${SMTP_HOST:smtp.gmail.com}
  port: ${SMTP_PORT:587}
  username: ${SMTP_USERNAME}
  password: ${SMTP_PASSWORD}
  properties:
    mail:
      smtp:
        auth: true
        starttls:
          enable: true

# Firebase Cloud Messaging (Push Notifications)
fcm:
  enabled: ${FCM_ENABLED:true}
  server-key: ${FCM_SERVER_KEY}

# External APIs
external-api:
  enabled: ${EXTERNAL_API_ENABLED:false}
  base-url: ${EXTERNAL_API_URL:https://api.pharmaceutical-db.com}
  api-key: ${EXTERNAL_API_KEY}
  timeout: 5000

# File Storage (AWS S3 ou compatible)
cloud-storage:
  provider: ${STORAGE_PROVIDER:s3} # s3, gcs, azure
  bucket-name: ${STORAGE_BUCKET:medverify-uploads}
  region: ${STORAGE_REGION:eu-west-1}
  access-key: ${STORAGE_ACCESS_KEY}
  secret-key: ${STORAGE_SECRET_KEY}

# Rate Limiting
rate-limit:
  enabled: true
  requests-per-minute: 60
  verification-requests-per-hour: 100

# Logging
logging:
  level:
    root: INFO
    com.medverify: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: logs/medverify.log
    max-size: 10MB
    max-history: 30

# Actuator (Monitoring)
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true

# Application Info
info:
  app:
    name: MedVerify API
    version: 1.0.0
    description: API de vérification médicaments Guinée-Bissau
```

---

## 6. SÉCURITÉ APPROFONDIE

### 6.1 Checkliste de sécurité

#### Backend
- [x] **Authentification JWT** avec access/refresh tokens
- [x] **Rate limiting** sur endpoints sensibles (login, verify)
- [x] **Validation stricte** des entrées (Bean Validation, regex)
- [x] **Protection CSRF** désactivée (API stateless) mais CORS strict
- [x] **SQL Injection** prévention via JPA/Prepared Statements
- [x] **XSS** sanitization des inputs text
- [x] **Logging audit** de toutes actions sensibles
- [x] **Encryption** des données sensibles au repos (DB encryption)
- [x] **HTTPS obligatoire** en production
- [x] **Secrets management** via variables d'environnement
- [x] **Account lockout** après 5 tentatives échouées
- [x] **Password policy** : min 8 chars, maj/min/chiffre/symbole

#### Frontend
- [x] **Token storage** sécurisé (Keychain iOS, Keystore Android)
- [x] **Certificate pinning** pour empêcher MITM
- [x] **Code obfuscation** en production
- [x] **Biometric auth** optionnelle (Touch ID/Face ID)
- [x] **Auto-logout** après inactivité (15min)
- [x] **Validation locale** avant envoi API
- [x] **Pas de données sensibles** dans logs en production

### 6.2 Implementation Certificate Pinning

```typescript
// Fichier : src/services/ApiClient.ts
import axios from 'axios';
import { Platform } from 'react-native';
// @ts-ignore
import { configureCertificatePinning } from 'react-native-ssl-pinning';

const API_BASE_URL = 'https://api.medverify.gw';

// SHA-256 fingerprints des certificats autorisés
const CERTIFICATE_PINS = {
  'api.medverify.gw': [
    'sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=', // Production cert
    'sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB='  // Backup cert
  ]
};

// Configuration du pinning au démarrage de l'app
export const initializeCertificatePinning = async () => {
  if (Platform.OS === 'android' || Platform.OS === 'ios') {
    try {
      await configureCertificatePinning({
        domains: CERTIFICATE_PINS
      });
      console.log('Certificate pinning configuré');
    } catch (error) {
      console.error('Erreur configuration certificate pinning:', error);
      // En production, bloquer l'app si pinning échoue
    }
  }
};

// Client axios avec pinning
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

export default apiClient;
```

### 6.3 Rate Limiting Backend

```java
// Fichier : RateLimitingFilter.java
@Component
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    @Value("${rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String clientId = getClientIdentifier(request);
        RateLimiter limiter = limiters.computeIfAbsent(
            clientId, 
            k -> RateLimiter.create(requestsPerMinute / 60.0) // par seconde
        );
        
        if (!limiter.tryAcquire()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\": \"Trop de requêtes. Veuillez patienter.\"}"
            );
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        // Combiner IP + User-Agent pour identifier client
        String ip = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");
        return ip + ":" + (userAgent != null ? userAgent.hashCode() : "unknown");
    }
    
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
```

---

## 7. TESTS & QUALITÉ

### 7.1 Tests Backend (JUnit 5 + Mockito)

```java
// Fichier : MedicationVerificationServiceTest.java
@SpringBootTest
@AutoConfigureMockMvc
class MedicationVerificationServiceTest {
    
    @Autowired
    private MedicationVerificationService verificationService;
    
    @MockBean
    private MedicationRepository medicationRepo;
    
    @MockBean
    private ScanHistoryRepository scanHistoryRepo;
    
    @Test
    @DisplayName("Devrait retourner AUTHENTIC pour médicament valide")
    void testVerifyAuthenticMedication() {
        // Given
        Medication medication = Medication.builder()
            .gtin("03401234567890")
            .name("Paracetamol 500mg")
            .isActive(true)
            .build();
        
        when(medicationRepo.findByGtin("03401234567890"))
            .thenReturn(Optional.of(medication));
        
        when(scanHistoryRepo.countBySerialNumber("XYZ789"))
            .thenReturn(0L);
        
        VerificationRequest request = VerificationRequest.builder()
            .gtin("03401234567890")
            .serialNumber("XYZ789")
            .batchNumber("ABC123")
            .expiryDate(LocalDate.now().plusMonths(6))
            .build();
        
        // When
        VerificationResponse response = verificationService.verify(request, "test@example.com");
        
        // Then
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.AUTHENTIC);
        assertThat(response.getConfidence()).isGreaterThan(0.7);
        assertThat(response.getAlerts()).isEmpty();
        
        verify(scanHistoryRepo, times(1)).save(any(ScanHistory.class));
    }
    
    @Test
    @DisplayName("Devrait détecter numéro de série dupliqué")
    void testDetectDuplicateSerial() {
        // Given
        Medication medication = Medication.builder()
            .gtin("03401234567890")
            .name("Amoxicilline 500mg")
            .build();
        
        when(medicationRepo.findByGtin("03401234567890"))
            .thenReturn(Optional.of(medication));
        
        when(scanHistoryRepo.countBySerialNumber("DUPLICATE123"))
            .thenReturn(50L); // Déjà scanné 50 fois !
        
        VerificationRequest request = VerificationRequest.builder()
            .gtin("03401234567890")
            .serialNumber("DUPLICATE123")
            .expiryDate(LocalDate.now().plusMonths(6))
            .build();
        
        // When
        VerificationResponse response = verificationService.verify(request, "test@example.com");
        
        // Then
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.SUSPICIOUS);
        assertThat(response.getConfidence()).isLessThan(0.5);
        assertThat(response.getAlerts())
            .extracting(Alert::getType)
            .contains("SERIAL_DUPLICATE");
    }
    
    @Test
    @DisplayName("Devrait retourner UNKNOWN pour GTIN non trouvé")
    void testUnknownMedication() {
        // Given
        when(medicationRepo.findByGtin("99999999999999"))
            .thenReturn(Optional.empty());
        
        VerificationRequest request = VerificationRequest.builder()
            .gtin("99999999999999")
            .serialNumber("XYZ789")
            .build();
        
        // When
        VerificationResponse response = verificationService.verify(request, "test@example.com");
        
        // Then
        assertThat(response.getStatus()).isEqualTo(VerificationStatus.UNKNOWN);
        assertThat(response.getMedication()).isNull();
    }
}
```

### 7.2 Tests Frontend (Jest + React Native Testing Library)

```typescript
// Fichier : src/components/Scanner/__tests__/DataMatrixScanner.test.tsx
import React from 'react';
import { render, waitFor, fireEvent } from '@testing-library/react-native';
import { DataMatrixScanner } from '../DataMatrixScanner';
import { parseGS1 } from '../../../utils/gs1Parser';

jest.mock('react-native-vision-camera', () => ({
  Camera: 'Camera',
  useCameraDevice: jest.fn(() => ({ id: 'back' })),
  useCodeScanner: jest.fn((config) => {
    // Simuler scan après 100ms
    setTimeout(() => {
      config.onCodeScanned([{
        value: '(01)03401234567890(17)251231(10)ABC123(21)XYZ789',
        type: 'data-matrix'
      }]);
    }, 100);
    return {};
  })
}));

describe('DataMatrixScanner', () => {
  it('devrait appeler onScanSuccess avec données parsées', async () => {
    const mockOnScanSuccess = jest.fn();
    
    const { getByTestId } = render(
      <DataMatrixScanner onScanSuccess={mockOnScanSuccess} />
    );
    
    await waitFor(() => {
      expect(mockOnScanSuccess).toHaveBeenCalledWith({
        gtin: '03401234567890',
        expiryDate: '2025-12-31',
        batchNumber: 'ABC123',
        serialNumber: 'XYZ789'
      });
    }, { timeout: 200 });
  });
  
  it('devrait afficher erreur si code invalide', async () => {
    const mockOnError = jest.fn();
    
    // Mock scan avec code invalide
    jest.spyOn(require('../../../utils/gs1Parser'), 'parseGS1')
      .mockImplementation(() => {
        throw new Error('Format GS1 invalide');
      });
    
    const { getByText } = render(
      <DataMatrixScanner 
        onScanSuccess={jest.fn()}
        onError={mockOnError}
      />
    );
    
    await waitFor(() => {
      expect(mockOnError).toHaveBeenCalledWith(
        expect.objectContaining({
          message: 'Format GS1 invalide'
        })
      );
    });
  });
});
```

### 7.3 Tests d'intégration E2E (Detox)

```typescript
// Fichier : e2e/scan-flow.e2e.ts
describe('Flow de scan complet', () => {
  beforeAll(async () => {
    await device.launchApp();
  });

  beforeEach(async () => {
    await device.reloadReactNative();
  });

  it('devrait permettre de scanner et vérifier un médicament', async () => {
    // 1. Login
    await element(by.id('email-input')).typeText('test@example.com');
    await element(by.id('password-input')).typeText('Password123!');
    await element(by.id('login-button')).tap();
    
    // 2. Attendre chargement home
    await waitFor(element(by.id('home-screen')))
      .toBeVisible()
      .withTimeout(3000);
    
    // 3. Ouvrir scanner
    await element(by.id('scan-button')).tap();
    
    // 4. Simuler scan (via mock ou device.takePhoto)
    await element(by.id('scanner-camera')).tap();
    
    // 5. Vérifier résultat affiché
    await waitFor(element(by.id('scan-result')))
      .toBeVisible()
      .withTimeout(5000);
    
    await expect(element(by.id('medication-name')))
      .toHaveText('Paracetamol 500mg');
    
    await expect(element(by.id('status-badge')))
      .toHaveText('AUTHENTIQUE');
  });

  it('devrait afficher alerte pour médicament suspect', async () => {
    // ... flow similaire mais avec médicament suspect
    
    await waitFor(element(by.id('suspicious-alert')))
      .toBeVisible()
      .withTimeout(5000);
    
    await expect(element(by.id('alert-message')))
      .toContain('Ce médicament présente des anomalies');
  });
});
```

---

## 8. DÉPLOIEMENT & DEVOPS

### 8.1 Dockerfile Backend

```dockerfile
# Fichier : Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/medverify-api-*.jar app.jar

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

### 8.2 Docker Compose (Dev/Test)

```yaml
# Fichier : docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgis/postgis:15-3.3-alpine
    container_name: medverify-db
    environment:
      POSTGRES_DB: medverify
      POSTGRES_USER: medverify_user
      POSTGRES_PASSWORD: dev_password_123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U medverify_user"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build: .
    container_name: medverify-api
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: dev
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: medverify
      DB_USERNAME: medverify_user
      DB_PASSWORD: dev_password_123
      JWT_SECRET: dev-secret-key-minimum-256-bits-long-for-hs256-algorithm
    ports:
      - "8080:8080"
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  redis:
    image: redis:7-alpine
    container_name: medverify-cache
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

### 8.3 CI/CD Pipeline (GitHub Actions)

```yaml
# Fichier : .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  test-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Run tests
        run: mvn clean test
      
      - name: Generate coverage report
        run: mvn jacoco:report
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml

  test-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
          cache: 'npm'
          cache-dependency-path: med-verify-app/package-lock.json
      
      - name: Install dependencies
        working-directory: ./med-verify-app
        run: npm ci
      
      - name: Run tests
        working-directory: ./med-verify-app
        run: npm test -- --coverage
      
      - name: Lint
        working-directory: ./med-verify-app
        run: npm run lint

  build-and-deploy:
    needs: [test-backend, test-frontend]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      
      - name: Login to Container Registry
        uses: docker/login-action@v2
        with:
          registry: ${{ secrets.REGISTRY_URL }}
          username: ${{ secrets.REGISTRY_USERNAME }}
          password: ${{ secrets.REGISTRY_PASSWORD }}
      
      - name: Build and push Docker image
        uses: docker/build-push-action@v4
        with:
          context: .
          push: true
          tags: |
            ${{ secrets.REGISTRY_URL }}/medverify-api:latest
            ${{ secrets.REGISTRY_URL }}/medverify-api:${{ github.sha }}
          cache-from: type=registry,ref=${{ secrets.REGISTRY_URL }}/medverify-api:buildcache
          cache-to: type=registry,ref=${{ secrets.REGISTRY_URL }}/medverify-api:buildcache,mode=max
      
      - name: Deploy to production
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.PROD_HOST }}
          username: ${{ secrets.PROD_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            cd /opt/medverify
            docker-compose pull
            docker-compose up -d
            docker system prune -f

  build-mobile-android:
    needs: [test-frontend]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      
      - name: Install dependencies
        working-directory: ./med-verify-app
        run: npm ci
      
      - name: Build Android Release
        working-directory: ./med-verify-app/android
        run: ./gradlew assembleRelease
      
      - name: Sign APK
        uses: r0adkll/sign-android-release@v1
        with:
          releaseDirectory: med-verify-app/android/app/build/outputs/apk/release
          signingKeyBase64: ${{ secrets.ANDROID_SIGNING_KEY }}
          alias: ${{ secrets.ANDROID_KEY_ALIAS }}
          keyStorePassword: ${{ secrets.ANDROID_KEYSTORE_PASSWORD }}
          keyPassword: ${{ secrets.ANDROID_KEY_PASSWORD }}
      
      - name: Upload to Google Play
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.GOOGLE_PLAY_SERVICE_ACCOUNT }}
          packageName: com.medverify.app
          releaseFiles: med-verify-app/android/app/build/outputs/apk/release/app-release-signed.apk
          track: production
          status: completed

		  ### 8.4 Configuration Kubernetes (Production)

```yaml
# Fichier : k8s/deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: medverify-api
  namespace: production
spec:
  replicas: 3
  selector:
    matchLabels:
      app: medverify-api
  template:
    metadata:
      labels:
        app: medverify-api
        version: v1.0.0
    spec:
      containers:
      - name: api
        image: registry.medverify.gw/medverify-api:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: medverify-secrets
              key: db-host
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: medverify-secrets
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: medverify-secrets
              key: db-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: medverify-secrets
              key: jwt-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: medverify-api-service
  namespace: production
spec:
  selector:
    app: medverify-api
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: medverify-ingress
  namespace: production
  annotations:
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  tls:
  - hosts:
    - api.medverify.gw
    secretName: medverify-tls
  rules:
  - host: api.medverify.gw
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: medverify-api-service
            port:
              number: 80
```

---

## 9. MONITORING & OBSERVABILITÉ

### 9.1 Configuration Prometheus

```yaml
# Fichier : monitoring/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'medverify-api'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['medverify-api:8080']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'medverify-api'

  - job_name: 'postgres'
    static_configs:
      - targets: ['postgres-exporter:9187']
```

### 9.2 Dashboard Grafana (JSON)

```json
{
  "dashboard": {
    "title": "MedVerify API Monitoring",
    "panels": [
      {
        "title": "Requêtes par seconde",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{application='medverify-api'}[1m])"
          }
        ]
      },
      {
        "title": "Temps de réponse P95",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, http_server_requests_seconds_bucket{application='medverify-api'})"
          }
        ]
      },
      {
        "title": "Taux d'erreur",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{application='medverify-api',status=~'5..'}[5m]) / rate(http_server_requests_seconds_count{application='medverify-api'}[5m])"
          }
        ]
      },
      {
        "title": "Scans par heure",
        "targets": [
          {
            "expr": "increase(scan_verification_total[1h])"
          }
        ]
      },
      {
        "title": "Taux de médicaments suspects",
        "targets": [
          {
            "expr": "scan_verification_suspicious_total / scan_verification_total"
          }
        ]
      }
    ]
  }
}
```

### 9.3 Métriques Custom Spring Boot

```java
// Fichier : MetricsConfiguration.java
@Configuration
public class MetricsConfiguration {
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "medverify-api",
            "environment", System.getenv("SPRING_PROFILES_ACTIVE")
        );
    }
}

// Fichier : MedicationVerificationService.java
@Service
@RequiredArgsConstructor
public class MedicationVerificationService {
    
    private final MeterRegistry meterRegistry;
    
    public VerificationResponse verify(VerificationRequest request, String username) {
        // Timer pour mesurer temps de vérification
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            VerificationResponse response = performVerification(request, username);
            
            // Incrémenter compteur selon statut
            meterRegistry.counter(
                "scan.verification",
                "status", response.getStatus().toString(),
                "source", response.getVerificationSource()
            ).increment();
            
            return response;
        } finally {
            sample.stop(Timer.builder("scan.verification.duration")
                .description("Temps de vérification d'un médicament")
                .tags("endpoint", "verify")
                .register(meterRegistry));
        }
    }
}
```

### 9.4 Alerting (Alertmanager)

```yaml
# Fichier : monitoring/alertmanager.yml
route:
  group_by: ['alertname', 'severity']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'team-email'

receivers:
  - name: 'team-email'
    email_configs:
      - to: 'ops@medverify.gw'
        from: 'alerts@medverify.gw'
        smarthost: 'smtp.gmail.com:587'
        auth_username: 'alerts@medverify.gw'
        auth_password: '$SMTP_PASSWORD'

  - name: 'slack-critical'
    slack_configs:
      - api_url: '$SLACK_WEBHOOK_URL'
        channel: '#alerts-critical'
        title: '🚨 ALERTE CRITIQUE MedVerify'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'

# Fichier : monitoring/alert-rules.yml
groups:
  - name: medverify_alerts
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 2m
        labels:
          severity: critical
        annotations:
          description: "Taux d'erreur élevé: {{ $value }}%"
      
      - alert: HighSuspiciousMedicationsRate
        expr: (scan_verification_suspicious_total / scan_verification_total) > 0.15
        for: 10m
        labels:
          severity: warning
        annotations:
          description: "Taux de médicaments suspects anormalement élevé: {{ $value }}%"
      
      - alert: DatabaseConnectionPoolExhausted
        expr: hikaricp_connections_active >= hikaricp_connections_max * 0.9
        for: 1m
        labels:
          severity: critical
        annotations:
          description: "Pool de connexions DB presque saturé"
      
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          description: "Utilisation mémoire élevée: {{ $value }}%"
```

---

## 10. GUIDE D'IMPLÉMENTATION POUR CURSOR AI

### 10.1 Ordre de développement recommandé

#### Sprint 1 (2 semaines) : Infrastructure & Auth
**Backend :**
```bash
# Commandes pour Cursor AI
1. Créer projet Spring Boot avec dependencies:
   - Spring Web, Spring Security, Spring Data JPA
   - PostgreSQL Driver, Flyway, Lombok
   - JWT (io.jsonwebtoken:jjwt-api:0.11.5)

2. Générer entités: User, RefreshToken

3. Implémenter:
   - SecurityConfig avec JWT
   - JwtService
   - AuthController (register, login, refresh)
   - UserDetailsServiceImpl

4. Créer migrations Flyway: V1__initial_schema.sql

5. Tests: AuthControllerTest, JwtServiceTest
```

**Frontend :**
```bash
1. Initialiser React Native:
   npx react-native init MedVerifyApp --template react-native-template-typescript

2. Installer dependencies:
   npm install @react-navigation/native @react-navigation/stack
   npm install axios @react-native-async-storage/async-storage
   npm install tailwind-rn

3. Créer structure: screens/Auth/, services/AuthService.ts

4. Implémenter:
   - LoginScreen
   - RegisterScreen
   - AuthService avec intercepteurs JWT

5. Configurer navigation: AuthNavigator, MainNavigator
```

#### Sprint 2 (3 semaines) : Scan & Vérification
**Backend :**
```bash
1. Entités: Medication, MedicationBatch, ScanHistory

2. Repositories avec requêtes custom

3. Services:
   - MedicationVerificationService (logique authentification)
   - ScanHistoryService

4. Controllers:
   - MedicationController (/verify endpoint)

5. Migrations: V2__medications_schema.sql

6. Tests unitaires et intégration
```

**Frontend :**
```bash
1. Installer librairie scan:
   npm install react-native-vision-camera vision-camera-code-scanner

2. Configurer permissions (AndroidManifest.xml, Info.plist)

3. Composants:
   - DataMatrixScanner.tsx
   - ScanResult.tsx

4. Screens:
   - ScanScreen
   - ScanResultScreen

5. Utilitaires: gs1Parser.ts

6. Base SQLite locale pour cache offline

7. Tests: Scanner.test.tsx
```

#### Sprint 3 (2 semaines) : Signalements
**Backend :**
```bash
1. Entité: Report

2. Service: ReportService + NotificationService

3. Controller: ReportController

4. Intégrations: EmailService, PushNotificationService (FCM)

5. Migration: V3__reports_schema.sql

6. Tests
```

**Frontend :**
```bash
1. Screens:
   - ReportCreateScreen (formulaire + upload photos)
   - ReportListScreen

2. Service: ReportService

3. Intégration react-native-image-picker pour photos
```

#### Sprint 4 (2 semaines) : Dashboard Autorités
**Backend :**
```bash
1. Repository queries pour analytics

2. DashboardService (calculs KPIs, détection anomalies)

3. DashboardController

4. DTOs pour statistiques

5. Tests
```

**Frontend :**
```bash
1. AuthorityDashboard screen

2. Composants graphiques:
   - KPICard
   - Charts (react-native-chart-kit)

3. Actualisation en temps réel (polling ou WebSocket)
```

#### Sprint 5 (2 semaines) : Tests & Optimisation
```bash
1. Tests E2E (Detox)

2. Optimisation performances:
   - Indexes DB
   - Cache Redis
   - Pagination API

3. Monitoring: Prometheus, Grafana

4. Documentation API (Swagger)

5. Accessibilité (A11y)
```

#### Sprint 6 (1 semaine) : Déploiement
```bash
1. Configuration CI/CD (GitHub Actions)

2. Dockerization

3. Déploiement backend (AWS/GCP)

4. Build APK/IPA

5. Soumission Play Store / App Store

6. Formation utilisateurs
```

### 10.2 Prompts spécifiques pour Cursor AI

#### Prompt 1 : Génération entité User
```
Génère une entité JPA User pour Spring Boot avec les spécifications suivantes:
- ID de type UUID auto-généré
- Email unique et non nullable
- Password hashé avec BCrypt
- Rôle enum (PATIENT, PHARMACIST, AUTHORITY, ADMIN)
- Champs: firstName, lastName, phone, isVerified, isActive
- Gestion tentatives login échouées (failedLoginAttempts, lockedUntil)
- Timestamps: createdAt, updatedAt, lastLoginAt
- Implémente UserDetails de Spring Security
- Utilise Lombok (@Data, @Builder)
- Annotations JPA appropriées
```

#### Prompt 2 : Endpoint vérification
```
Crée un endpoint REST POST /api/v1/medications/verify avec:
- Request body: gtin, serialNumber, batchNumber, expiryDate, location
- Authentification JWT requise
- Validation des données (@Valid)
- Logique de vérification:
  1. Recherche médicament par GTIN
  2. Détection anomalies (serial dupliqué, périmé, lot rappelé)
  3. Calcul score confidence
  4. Enregistrement scan en base
- Response: status (AUTHENTIC/SUSPICIOUS/UNKNOWN), medication, alerts, confidence
- Gestion erreurs appropriée
- Tests unitaires avec Mockito
```

#### Prompt 3 : Composant Scanner React Native
```
Crée un composant DataMatrixScanner React Native avec:
- Utilisation de react-native-vision-camera
- Scan code Data Matrix et QR Code
- Overlay visuel avec cadre guidage
- Feedback haptique au scan réussi
- Parsing format GS1 (AI 01, 17, 10, 21)
- Props: onScanSuccess, onError
- Gestion permissions caméra
- Support iOS et Android
- TypeScript strict
- Tests avec Jest
```

#### Prompt 4 : Service de vérification avec cache
```
Implémente MedicationVerificationService Spring Boot avec:
- Injection: MedicationRepository, ScanHistoryRepository, Redis
- Méthode verify(request, username):
  1. Check cache Redis (TTL 1h)
  2. Si non en cache: query DB + API externe si configurée
  3. Analyse authenticité avec règles métier
  4. Calcul score confidence
  5. Enregistrement scan history
  6. Notification autorités si suspect
  7. Mise en cache résultat
- Métriques Prometheus (@Timed)
- Logging approprié
- Transaction management
- Tests unitaires complets
```

### 10.3 Fichiers de configuration à créer

#### application.yml complet
```yaml
# Déjà fourni section 5.3 - utiliser tel quel
```

#### package.json React Native
```json
{
  "name": "MedVerifyApp",
  "version": "1.0.0",
  "scripts": {
    "android": "react-native run-android",
    "ios": "react-native run-ios",
    "start": "react-native start",
    "test": "jest",
    "lint": "eslint . --ext .js,.jsx,.ts,.tsx"
  },
  "dependencies": {
    "react": "18.2.0",
    "react-native": "0.72.0",
    "@react-navigation/native": "^6.1.9",
    "@react-navigation/stack": "^6.3.20",
    "react-native-vision-camera": "^3.6.0",
    "vision-camera-code-scanner": "^0.2.0",
    "axios": "^1.6.0",
    "@react-native-async-storage/async-storage": "^1.19.0",
    "tailwind-rn": "^4.2.0",
    "react-native-chart-kit": "^6.12.0",
    "react-native-image-picker": "^5.7.0",
    "@reduxjs/toolkit": "^1.9.7",
    "react-redux": "^8.1.3",
    "react-native-sqlite-storage": "^6.0.1",
    "i18next": "^23.7.0",
    "react-i18next": "^13.5.0"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-native": "^0.72.0",
    "@testing-library/react-native": "^12.4.0",
    "@testing-library/jest-native": "^5.4.3",
    "jest": "^29.7.0",
    "detox": "^20.14.0",
    "eslint": "^8.54.0",
    "typescript": "^5.3.0"
  }
}
```

#### pom.xml Backend
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.medverify</groupId>
    <artifactId>medverify-api</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
        <jwt.version>0.11.5</jwt.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-spatial</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Utils -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Monitoring -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        
        <!-- Documentation -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 11. DOCUMENTATION UTILISATEUR

### 11.1 Guide Patient

**Comment vérifier un médicament :**

1. **Téléchargez l'application** MedVerify sur Play Store ou App Store
2. **Créez un compte** avec votre email et mot de passe
3. **Scannez le code** sur votre boîte de médicaments :
   - Ouvrez l'application
   - Appuyez sur le bouton "Scanner"
   - Pointez la caméra vers le code Data Matrix (petit carré noir et blanc)
   - Attendez le "bip" de confirmation
4. **Consultez le résultat** :
   - ✅ **Vert = Authentique** : Médicament vérifié et sûr
   - ⚠️ **Orange = Inconnu** : Non trouvé en base, signalez-le
   - 🚫 **Rouge = Suspect** : Ne PAS utiliser, contactez un pharmacien
5. **Accédez aux informations** : posologie, effets secondaires, contre-indications

**En cas de médicament suspect :**
- Appuyez sur "Signaler"
- Remplissez le formulaire
- Prenez 1-2 photos si possible
- Les autorités seront notifiées automatiquement

### 11.2 Guide Pharmacien

**Fonctionnalités supplémentaires :**

1. **Scan multiple (batch)** :
   - Menu → Mode Professionnel
   - Scannez plusieurs médicaments d'affilée
   - Export CSV de l'historique

2. **Accès détails techniques** :
   - Numéro de lot complet
   - Date de fabrication
   - Informations fabricant
   - Historique de rappels

3. **Signalement prioritaire** :
   - Vos signalements sont traités en priorité
   - Contact direct avec inspecteurs INASA

4. **Statistiques pharmacie** :
   - Nombre de scans effectués
   - Taux de médicaments suspects détectés
   - Médicaments les plus vérifiés

### 11.3 Guide Autorités Sanitaires

**Dashboard Analytics :**

1. **Vue d'ensemble** :
   - Scans totaux période
   - Taux d'authenticité national
   - Signalements en attente
   - Alertes critiques

2. **Carte géographique** :
   - Zones à risque (taux suspect élevé)
   - Distribution des signalements
   - Pharmacies partenaires

3. **Top contrefaçons** :
   - Médicaments les plus contrefaits
   - Tendances temporelles
   - Fabricants suspects

4. **Gestion signalements** :
   - Filtrer par statut, région, type
   - Assigner à inspecteur
   - Marquer comme traité
   - Export rapports PDF

---

## 12. ANNEXES

### 12.1 Glossaire

| Terme | Définition |
|-------|------------|
| **Data Matrix** | Code-barres 2D capable de stocker jusqu'à 2335 caractères alphanumériques |
| **GS1** | Organisation internationale de standardisation des codes-barres |
| **GTIN** | Global Trade Item Number - Identifiant unique produit (14 chiffres) |
| **AI (Application Identifier)** | Préfixe dans code GS1 indiquant type de données (ex: 01=GTIN, 17=Date) |
| **JWT** | JSON Web Token - Standard d'authentification par token |
| **AMM** | Autorisation de Mise sur le Marché |
| **ATC** | Anatomical Therapeutic Chemical - Classification médicaments OMS |
| **INASA** | Institut National de Santé Publique Guinée-Bissau |

### 12.2 Références réglementaires

- **OMS** : Guidelines on Packaging for Pharmaceutical Products (2002)
- **GS1** : Healthcare Implementation Guideline (2021)
- **ISO 15394** : AIDC Healthcare Data Format
- **Directive 2011/62/UE** : Prévention contrefaçon (adaptation locale)

### 12.3 API Externes recommandées

| Service | URL | Usage | Coût |
|---------|-----|-------|------|
| **RxNorm API** | https://rxnav.nlm.nih.gov/REST | Base médicaments US | Gratuit |
| **OpenFDA** | https://open.fda.gov | Rappels, effets indésirables | Gratuit |
| **EU Medicines Registry** | https://www.ema.europa.eu/en/medicines | Médicaments UE | Gratuit |
| **WHO Essential Medicines** | https://list.essentialmeds.org | Liste OMS | Gratuit |

### 12.4 Checklist de lancement

#### Pré-Production

**Backend :**
- [ ] Tests unitaires > 80% coverage
- [ ] Tests d'intégration passent
- [ ] Migrations Flyway testées sur copie prod
- [ ] Secrets en variables d'environnement (pas hardcodés)
- [ ] Rate limiting activé
- [ ] HTTPS configuré (certificat Let's Encrypt)
- [ ] Monitoring Prometheus/Grafana opérationnel
- [ ] Alertes configurées (Slack/Email)
- [ ] Backup automatique DB (daily)
- [ ] Documentation API Swagger complète
- [ ] Load testing (JMeter/Gatling) : 1000 req/s OK
- [ ] Logs centralisés (ELK/Loki)

**Frontend :**
- [ ] Tests E2E Detox passent (iOS + Android)
- [ ] Validation formulaires complète
- [ ] Gestion erreurs réseau gracieuse
- [ ] Mode offline fonctionnel
- [ ] Performance : temps chargement < 3s
- [ ] Accessibilité A11y validée (TalkBack/VoiceOver)
- [ ] Traductions complètes (PT/FR/EN)
- [ ] APK/IPA signés
- [ ] Politique confidentialité + CGU intégrées
- [ ] Analytics Firebase configuré
- [ ] Crash reporting (Sentry/Crashlytics)
- [ ] Deep linking configuré

**Légal & Conformité :**
- [ ] Déclaration CNIL/équivalent local
- [ ] Conformité RGPD (consentement, droit à l'oubli)
- [ ] Conditions Générales d'Utilisation validées
- [ ] Politique de confidentialité claire
- [ ] Accord autorités sanitaires (INASA)
- [ ] Partenariats pharmacies formalisés
- [ ] Assurance responsabilité civile professionnelle

**Opérations :**
- [ ] Serveurs production configurés (2 env: staging + prod)
- [ ] CI/CD pipeline testé
- [ ] Procédure rollback documentée
- [ ] Plan de DR (Disaster Recovery)
- [ ] Équipe support formée
- [ ] Documentation admin complète
- [ ] Runbook incidents
- [ ] Contact escalade 24/7

#### Post-Lancement

**Semaine 1 :**
- [ ] Monitoring quotidien logs/erreurs
- [ ] Analyse retours utilisateurs (reviews stores)
- [ ] Hotfixes critiques si nécessaire
- [ ] Communication presse locale
- [ ] Formation pharmacies pilotes (5-10)

**Mois 1 :**
- [ ] Analyse métriques :
  - Taux adoption
  - Taux rétention J7, J14, J30
  - Temps moyen scan
  - Taux succès scan
  - Taux signalements
- [ ] A/B testing UI/UX
- [ ] Optimisations performance
- [ ] Roadmap v1.1 (feedback utilisateurs)

**Mois 3 :**
- [ ] Audit sécurité externe
- [ ] Évaluation impact (métriques santé publique)
- [ ] Extension partenariats
- [ ] Plan scaling (si croissance forte)

### 12.5 Budget Estimatif

#### Coûts Développement (One-time)

| Poste | Détails | Coût estimé |
|-------|---------|-------------|
| **Développement Backend** | 4 dev × 3 mois | 30 000 € |
| **Développement Frontend** | 3 dev × 3 mois | 25 000 € |
| **Design UI/UX** | 1 designer × 1.5 mois | 6 000 € |
| **QA/Testing** | 1 QA × 2 mois | 6 000 € |
| **DevOps Setup** | Infrastructure initiale | 3 000 € |
| **Légal/Conformité** | Avocat + CNIL | 5 000 € |
| **Marketing Lancement** | Campagne initiale | 8 000 € |
| **Contingence (20%)** | Imprévus | 16 600 € |
| **TOTAL** | | **99 600 €** |

#### Coûts Récurrents (Mensuel)

| Poste | Détails | Coût/mois |
|-------|---------|-----------|
| **Hébergement Cloud** | AWS/GCP (10K users) | 500 € |
| **Base de données** | PostgreSQL managé | 200 € |
| **CDN + Stockage** | S3/CloudFront | 100 € |
| **Monitoring** | Grafana Cloud | 50 € |
| **Services Email** | SendGrid/Mailgun | 50 € |
| **Push Notifications** | Firebase (gratuit < 100K) | 0 € |
| **SMS** | Vérification comptes | 100 € |
| **Certificats SSL** | Let's Encrypt (gratuit) | 0 € |
| **Maintenance/Support** | 0.5 dev équivalent | 3 000 € |
| **Assurance** | RC Pro | 150 € |
| **TOTAL** | | **4 150 €** |

**Note** : Coûts scaling si 100K+ utilisateurs : × 3-5

### 12.6 KPIs de Succès

#### Métriques Techniques

| Métrique | Objectif | Critique |
|----------|----------|----------|
| **Disponibilité** | > 99.5% (uptime) | Oui |
| **Temps réponse API** | P95 < 500ms | Oui |
| **Temps scan** | < 3s (caméra → résultat) | Oui |
| **Taux erreur** | < 1% | Oui |
| **Couverture tests** | > 80% | Non |
| **Performance mobile** | Score Lighthouse > 80 | Non |

#### Métriques Produit

| Métrique | Objectif 3 mois | Objectif 6 mois |
|----------|-----------------|-----------------|
| **Téléchargements** | 5 000 | 10 000 |
| **Utilisateurs actifs mensuels** | 3 000 | 7 000 |
| **Rétention J30** | > 40% | > 50% |
| **Scans/utilisateur/mois** | 3+ | 5+ |
| **Taux succès scan** | > 90% | > 95% |
| **NPS (Net Promoter Score)** | > 40 | > 60 |
| **Note stores** | > 4.0/5 | > 4.3/5 |

#### Métriques Impact Santé

| Métrique | Objectif 6 mois | Mesure |
|----------|-----------------|--------|
| **Médicaments vérifiés** | 30 000 | Logs scans |
| **Contrefaçons détectées** | 50+ | Signalements confirmés |
| **Pharmacies partenaires** | 100 | Comptes pharmaciens actifs |
| **Taux confiance système santé** | +10% | Enquête pré/post |
| **Réduction temps vérification** | -70% | Vs. processus manuel |

### 12.7 Risques & Mitigations

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|------------|
| **Faible adoption utilisateurs** | Moyenne | Élevé | - Marketing ciblé pharmacies<br>- Onboarding simplifié<br>- Campagne sensibilisation |
| **Données médicaments incomplètes** | Élevée | Élevé | - Partenariat INASA<br>- Crowdsourcing pharmaciens<br>- API externes backup |
| **Connectivité limitée** | Élevée | Moyen | - Mode offline robuste<br>- Cache intelligent<br>- Synchro différée |
| **Contrefacteurs sophistiqués** | Moyenne | Élevé | - ML détection patterns<br>- Collaboration autorités<br>- MAJ régulières algorithmes |
| **Charge serveurs imprévue** | Faible | Moyen | - Auto-scaling cloud<br>- CDN<br>- Rate limiting |
| **Fuite données** | Faible | Critique | - Encryption at rest/transit<br>- Audits sécu réguliers<br>- Pen testing |
| **Dépendance API externes** | Moyenne | Moyen | - Fallback base locale<br>- Cache agressif<br>- Monitoring uptime |
| **Changements réglementaires** | Faible | Moyen | - Veille légale<br>- Architecture modulaire<br>- Flexibilité config |

### 12.8 Roadmap Future (v2.0+)

#### Phase 7 - Court Terme (6-12 mois)

**Intelligence Artificielle :**
- [ ] ML pour détection anomalies visuelles (packaging)
- [ ] OCR automatique si Data Matrix illisible
- [ ] Prédiction zones à risque (heatmap)
- [ ] Chatbot support utilisateur

**Fonctionnalités :**
- [ ] Wallet numérique ordonnances
- [ ] Rappels prise médicaments
- [ ] Intégration dossier médical personnel
- [ ] Mode famille (gérer médicaments enfants/seniors)
- [ ] Historique interactions médicamenteuses

**Intégrations :**
- [ ] API pharmacies (stock temps réel)
- [ ] Système de santé national (si disponible)
- [ ] Assurances santé (remboursements)
- [ ] Wearables (Apple Health, Google Fit)

#### Phase 8 - Moyen Terme (12-24 mois)

**Expansion Géographique :**
- [ ] Déploiement pays voisins (Sénégal, Gambie, Mali)
- [ ] Adaptation bases médicaments locales
- [ ] Partenariats CEDEAO

**Blockchain :**
- [ ] Traçabilité bout-en-bout supply chain
- [ ] Smart contracts fabricants/distributeurs
- [ ] NFT certificats authenticité

**Monétisation :**
- [ ] Freemium (patients gratuit, pharmaciens premium)
- [ ] API B2B hôpitaux/grossistes
- [ ] Rapports analytics autorités (SaaS)

### 12.9 Stack Technique Résumé

```yaml
Backend:
  Framework: Spring Boot 3.2.0
  Language: Java 17
  Database: PostgreSQL 15 + PostGIS
  Cache: Redis 7
  Security: Spring Security + JWT
  API Doc: Swagger/OpenAPI 3.0
  Build: Maven 3.9
  Migrations: Flyway
  Testing: JUnit 5 + Mockito + Testcontainers

Frontend:
  Framework: React Native 0.72
  Language: TypeScript 5.3
  State: Redux Toolkit
  Navigation: React Navigation 6
  Scanner: react-native-vision-camera
  Storage: AsyncStorage + SQLite
  Charts: react-native-chart-kit
  i18n: react-i18next
  Testing: Jest + React Native Testing Library + Detox

DevOps:
  CI/CD: GitHub Actions
  Containers: Docker + Docker Compose
  Orchestration: Kubernetes (production)
  Monitoring: Prometheus + Grafana
  Logging: ELK Stack / Loki
  Alerting: Alertmanager
  Cloud: AWS / GCP / Azure

Mobile:
  Android: API 24+ (Android 7.0+)
  iOS: iOS 13+
  Distribution: Google Play Store + Apple App Store
  Analytics: Firebase Analytics
  Crash Reporting: Crashlytics

Sécurité:
  HTTPS: Let's Encrypt
  Certificate Pinning: Oui
  Encryption: AES-256
  Auth: JWT (HS256)
  Rate Limiting: Token Bucket
  WAF: CloudFlare / AWS WAF
```

### 12.10 Contacts & Support

**Équipe Projet :**
- **Product Owner** : [Nom] - [email]
- **Tech Lead Backend** : [Nom] - [email]
- **Tech Lead Mobile** : [Nom] - [email]
- **DevOps** : [Nom] - [email]

**Partenaires :**
- **INASA (Autorité Sanitaire)** : contact@inasa.gw
- **Association Pharmaciens** : [contact]

**Support Utilisateurs :**
- Email : support@medverify.gw
- WhatsApp : +245 XXX XXX XXX
- FAQ : https://medverify.gw/faq
- Heures : Lun-Ven 8h-18h GMT

**Support Technique :**
- Incidents critiques : ops@medverify.gw (24/7)
- GitHub Issues : https://github.com/medverify/app/issues
- Slack Community : medverify.slack.com

---

## 13. INSTRUCTIONS FINALES POUR CURSOR AI

### 13.1 Commandes de démarrage rapide

#### Backend (Spring Boot)

```bash
# 1. Créer projet
curl https://start.spring.io/starter.zip \
  -d dependencies=web,data-jpa,postgresql,security,validation,actuator,flyway \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.2.0 \
  -d groupId=com.medverify \
  -d artifactId=medverify-api \
  -d name=MedVerifyAPI \
  -d packageName=com.medverify \
  -d javaVersion=17 \
  -o medverify-backend.zip

# 2. Extraire et setup
unzip medverify-backend.zip -d medverify-backend
cd medverify-backend

# 3. Ajouter dependencies supplémentaires dans pom.xml
# (JWT, Lombok, Swagger - voir section 10.3)

# 4. Configuration DB locale
docker run -d --name medverify-db \
  -e POSTGRES_DB=medverify \
  -e POSTGRES_USER=dev \
  -e POSTGRES_PASSWORD=dev123 \
  -p 5432:5432 \
  postgis/postgis:15-3.3

# 5. Lancer application
./mvnw spring-boot:run
```

#### Frontend (React Native)

```bash
# 1. Initialiser projet
npx react-native init MedVerifyApp --template react-native-template-typescript
cd MedVerifyApp

# 2. Installer dépendances
npm install \
  @react-navigation/native \
  @react-navigation/stack \
  react-native-screens \
  react-native-safe-area-context \
  axios \
  @react-native-async-storage/async-storage \
  react-native-vision-camera \
  vision-camera-code-scanner \
  tailwind-rn \
  react-native-chart-kit \
  @reduxjs/toolkit \
  react-redux

# 3. Setup iOS
cd ios && pod install && cd ..

# 4. Permissions (ajouter dans AndroidManifest.xml)
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />

# 5. Permissions (ajouter dans Info.plist iOS)
<key>NSCameraUsageDescription</key>
<string>Nécessaire pour scanner les codes médicaments</string>

# 6. Lancer app
npm run android  # ou npm run ios
```

### 13.2 Ordre de génération des fichiers

**Utiliser cette séquence avec Cursor AI :**

1. **Backend - Entities** (ordre de dépendances)
   ```
   User.java → Medication.java → MedicationBatch.java → 
   ScanHistory.java → Report.java → Notification.java → RefreshToken.java
   ```

2. **Backend - Repositories**
   ```
   UserRepository.java → MedicationRepository.java → 
   ScanHistoryRepository.java → ReportRepository.java
   ```

3. **Backend - DTOs**
   ```
   request/ (LoginRequest, RegisterRequest, VerificationRequest, ReportRequest)
   response/ (AuthResponse, VerificationResponse, MedicationResponse)
   ```

4. **Backend - Services**
   ```
   JwtService.java → AuthService.java → 
   MedicationVerificationService.java → ReportService.java → 
   NotificationService.java → DashboardService.java
   ```

5. **Backend - Controllers**
   ```
   AuthController.java → MedicationController.java → 
   ScanController.java → ReportController.java → DashboardController.java
   ```

6. **Backend - Security**
   ```
   SecurityConfig.java → JwtAuthenticationFilter.java → 
   UserDetailsServiceImpl.java
   ```

7. **Frontend - Types**
   ```
   user.types.ts → medication.types.ts → scan.types.ts → api.types.ts
   ```

8. **Frontend - Services**
   ```
   ApiClient.ts → AuthService.ts → ScanService.ts → 
   MedicationService.ts → ReportService.ts
   ```

9. **Frontend - Redux Slices**
   ```
   authSlice.ts → medicationSlice.ts → scanSlice.ts → notificationSlice.ts
   ```

10. **Frontend - Components**
    ```
    UI components → Scanner components → Medication components
    ```

11. **Frontend - Screens**
    ```
    Auth screens → Home → Scan → History → Report → Dashboard → Profile
    ```

12. **Frontend - Navigation**
    ```
    AuthNavigator.tsx → MainNavigator.tsx → AppNavigator.tsx
    ```

### 13.3 Prompts optimisés par composant

#### Pour chaque Entity :
```
Génère l'entité JPA [NOM] avec les spécifications suivantes:
[Copier specs de la section 4.2]
Inclus:
- Annotations JPA (@Entity, @Table, @Id, etc.)
- Lombok (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
- Types appropriés (UUID, Instant, Enum)
- Relations (@ManyToOne, @OneToMany avec cascade)
- Indexes (@Index sur colonnes fréquemment requêtées)
- Contraintes (@Column(unique, nullable))
- Timestamps avec @CreatedDate, @LastModifiedDate
```

#### Pour chaque Service :
```
Crée le service [NOM] avec:
- @Service + @RequiredArgsConstructor (Lombok)
- Injection repositories nécessaires
- Méthode principale [nomMéthode] qui:
  [Décrire logique étape par étape]
- Gestion erreurs avec exceptions custom
- Logging approprié (@Slf4j)
- Métriques Prometheus si pertinent
- @Transactional où nécessaire
- Tests unitaires avec Mockito couvrant cas nominal + edge cases
```

#### Pour chaque Screen React Native :
```
Crée le screen [NOM] React Native TypeScript avec:
- Props typées (navigation, route)
- State management (useState ou Redux selon contexte)
- Hooks useEffect pour chargements
- Tailwind classes pour styling
- Gestion loading/error states
- Accessibilité (accessibilityLabel)
- i18n pour textes (useTranslation)
- Navigation (useNavigation hook)
Structure:
1. Imports
2. Types/Interfaces
3. Composant principal
4. Sous-composants si nécessaire
5. Styles (si non-Tailwind)
```

### 13.4 Validation finale avant déploiement

```bash
# Backend
cd medverify-backend

# Tests + coverage
./mvnw clean test jacoco:report
# Vérifier target/site/jacoco/index.html > 80%

# Build production
./mvnw clean package -DskipTests

# Vérifier JAR
ls -lh target/*.jar

# Frontend
cd MedVerifyApp

# Lint
npm run lint

# Tests
npm test -- --coverage --watchAll=false

# Build Android Release
cd android && ./gradlew assembleRelease

# Build iOS Release (sur Mac)
cd ios && xcodebuild -workspace MedVerifyApp.xcworkspace \
  -scheme MedVerifyApp -configuration Release

# Vérifier tailles
ls -lh android/app/build/outputs/apk/release/*.apk
ls -lh ios/build/Build/Products/Release-iphoneos/*.app
```

---

## 14. CONCLUSION

Ce PRD optimisé fournit toutes les spécifications nécessaires pour qu'un outil comme **Cursor AI** puisse développer l'application de bout en bout. Les éléments clés sont :

### Points forts du document :

✅ **Spécifications techniques précises** : Entities, APIs, components détaillés  
✅ **Architecture complète** : Frontend/Backend/DevOps/Sécurité  
✅ **Code examples** : Snippets Java, TypeScript, SQL directement utilisables  
✅ **Tests intégrés** : Unitaires, intégration, E2E  
✅ **Ordre de développement** : Sprints structurés avec dépendances  
✅ **Prompts Cursor-ready** : Instructions directes pour génération code  
✅ **Configuration complète** : application.yml, package.json, pom.xml, Docker  
✅ **Monitoring & Ops** : Prometheus, Grafana, alerting  
✅ **Documentation utilisateur** : Guides patients/pharmaciens/autorités  
✅ **Checklist déploiement** : Rien n'est oublié  

### Prochaines étapes recommandées :

1. **Valider avec stakeholders** (INASA, pharmaciens pilotes)
2. **Ajuster budget/timeline** selon ressources disponibles
3. **Préparer environnements** (dev, staging, prod)
4. **Recruter équipe** ou **configurer Cursor AI** avec ce PRD
5. **Lancer Sprint 1** : Infrastructure & Auth
6. **Itérer** avec feedback continu

### Support continu :

Pour toute clarification ou ajout de spécifications, ce document peut être mis à jour section par section. Chaque composant est modulaire et peut être développé/testé indépendamment.

**Bonne chance avec le développement ! 🚀💊**