# 🔍 Analyse - Fonctionnalités Pharmacies Manquantes

**Date :** 14 octobre 2025  
**Statut actuel :** 60% implémenté

---

## ✅ Ce qui est IMPLÉMENTÉ (100%)

### Backend ✅

- ✅ **Entities JPA complètes**

  - `Pharmacy.java` avec PostGIS
  - `OnDutySchedule.java`
  - `OpeningHours.java` / `OpeningHoursDay.java`
  - `DutyType.java` enum

- ✅ **Repositories PostGIS**

  - `PharmacyRepository.java` avec requêtes géospatiales (ST_Distance, ST_DWithin)
  - `OnDutyScheduleRepository.java`

- ✅ **Service & Controller**

  - `PharmacyService.java` - Logique métier complète
  - `PharmacyController.java` - 5 endpoints REST
  - DTOs (`PharmacyDTO`, `PharmacySearchRequest`)

- ✅ **Base de données**
  - Migration V10 avec PostGIS
  - Tables créées (pharmacies, on_duty_schedules, pharmacy_services)
  - Index géospatiaux GIST
  - 3 pharmacies de test à Bissau

### Frontend ✅

- ✅ **Services**

  - `GeolocationService.ts` (expo-location)
  - `PharmacyService.ts` (API calls)

- ✅ **Hooks**

  - `usePharmacies.ts` - Recherche pharmacies
  - `usePharmacyDetails.ts` - Détails pharmacie

- ✅ **Écrans**

  - `PharmaciesScreen.tsx` - Liste avec filtres
  - `PharmacyDetailScreen.tsx` - Détails complets

- ✅ **Navigation**

  - `PharmaciesNavigator.tsx`
  - Onglet "Pharmacies" 🏥 dans MainNavigator

- ✅ **Fonctionnalités de base**
  - Recherche par géolocalisation
  - Recherche par ville
  - Filtres (Toutes / Ouvertes / Garde / 24h)
  - Affichage distance
  - Badges statut
  - Itinéraire (Google Maps/Apple Maps)
  - Appel direct

---

## ❌ Ce qui MANQUE à implémenter

### 🗺️ 1. CARTE INTERACTIVE (Priorité HAUTE)

**Statut :** ❌ NON IMPLÉMENTÉ

**Problème :**

- Le guide mentionne `react-native-maps` mais :
  - ❌ La dépendance n'est pas installée dans `package.json`
  - ❌ Aucun composant `MapView` dans le code
  - ❌ PharmaciesScreen utilise UNIQUEMENT une `FlatList`
  - ❌ Pas de markers sur carte
  - ❌ Pas de toggle vue Liste/Carte

**Ce qui devrait être fait :**

#### Frontend - Dépendances manquantes

```bash
# À installer
npx expo install react-native-maps
npm install geolib
npm install --save-dev @types/geolib
```

#### Frontend - Composant MapView à créer

```typescript
// Dans PharmaciesScreen.tsx - À AJOUTER
import MapView, { Marker } from "react-native-maps";

const [viewMode, setViewMode] = useState<"list" | "map">("list");

// Vue Carte
<MapView
  initialRegion={{
    latitude: userLocation.latitude,
    longitude: userLocation.longitude,
    latitudeDelta: 0.05,
    longitudeDelta: 0.05,
  }}
  style={{ flex: 1 }}
>
  {pharmacies.map((pharmacy) => (
    <Marker
      key={pharmacy.id}
      coordinate={{
        latitude: pharmacy.latitude,
        longitude: pharmacy.longitude,
      }}
      title={pharmacy.name}
      onPress={() =>
        navigation.navigate("PharmacyDetail", { pharmacyId: pharmacy.id })
      }
    />
  ))}
</MapView>;
```

#### Frontend - Toggle Liste/Carte à créer

```typescript
// Bouton bascule à ajouter dans le header
<View style={styles.viewToggle}>
  <TouchableOpacity onPress={() => setViewMode("list")}>
    <Text>📋 Liste</Text>
  </TouchableOpacity>
  <TouchableOpacity onPress={() => setViewMode("map")}>
    <Text>🗺️ Carte</Text>
  </TouchableOpacity>
</View>
```

#### Configuration - Google Maps API Key

```json
// app.json - À CONFIGURER
{
  "expo": {
    "android": {
      "config": {
        "googleMaps": {
          "apiKey": "VOTRE_GOOGLE_MAPS_API_KEY"
        }
      }
    },
    "ios": {
      "config": {
        "googleMapsApiKey": "VOTRE_GOOGLE_MAPS_API_KEY"
      }
    }
  }
}
```

**Fichiers à modifier :**

- [ ] `package.json` - Ajouter dépendances
- [ ] `PharmaciesScreen.tsx` - Ajouter MapView + toggle
- [ ] `app.json` - Ajouter Google Maps API Key

---

### 📢 2. NOTIFICATIONS PUSH GARDES (Priorité MOYENNE)

**Statut :** ❌ NON IMPLÉMENTÉ (structure manquante)

**Problème :**

- Le guide mentionne `PharmacyDutyNotificationService.java` mais :
  - ❌ Le fichier n'existe PAS dans le backend
  - ❌ Aucun @Scheduled configuré
  - ❌ Pas d'intégration FCM pour cette fonctionnalité
  - ❌ Pas de notifications automatiques garde weekend

**Ce qui devrait être fait :**

#### Backend - Service de notifications à créer

```java
// medverify-backend/src/main/java/com/medverify/service/notification/
// PharmacyDutyNotificationService.java - À CRÉER

@Service
@Slf4j
public class PharmacyDutyNotificationService {

    @Autowired
    private OnDutyScheduleRepository onDutyScheduleRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    // Notification automatique vendredi 18h pour garde weekend
    @Scheduled(cron = "0 0 18 * * FRI")
    public void notifyWeekendDutyPharmacies() {
        log.info("Sending weekend duty pharmacy notifications...");

        LocalDate saturday = LocalDate.now().plusDays(1);
        LocalDate sunday = saturday.plusDays(1);

        List<OnDutySchedule> weekendDuties = onDutyScheduleRepository
            .findActiveSchedulesForDate(saturday);

        // Pour chaque région
        weekendDuties.stream()
            .collect(Collectors.groupingBy(OnDutySchedule::getRegion))
            .forEach((region, schedules) -> {
                // Récupérer utilisateurs de la région
                List<User> users = userRepository.findActiveUsersInRegion(region);

                schedules.forEach(schedule -> {
                    Pharmacy pharmacy = schedule.getPharmacy();

                    users.forEach(user -> {
                        notificationService.sendNotification(
                            user,
                            "Pharmacie de garde ce weekend",
                            pharmacy.getName() + " sera de garde ce weekend dans " + region,
                            NotificationType.PHARMACY_DUTY_UPDATE
                        );
                    });
                });
            });
    }

    // Notification urgente changement de garde
    public void notifyDutyChange(OnDutySchedule newDuty, String region) {
        List<User> users = userRepository.findActiveUsersInRegion(region);

        users.forEach(user -> {
            notificationService.sendNotification(
                user,
                "⚠️ Changement pharmacie de garde",
                "Nouvelle pharmacie de garde : " + newDuty.getPharmacy().getName(),
                NotificationType.PHARMACY_DUTY_UPDATE
            );
        });
    }
}
```

#### Backend - Enum NotificationType à mettre à jour

```java
// medverify-backend/src/main/java/com/medverify/entity/NotificationType.java
// À AJOUTER

public enum NotificationType {
    VERIFICATION_CODE,
    SCAN_RESULT,
    MEDICATION_ALERT,
    SYSTEM_ANNOUNCEMENT,
    PHARMACY_DUTY_UPDATE  // ← À AJOUTER
}
```

#### Backend - Repository à mettre à jour

```java
// medverify-backend/src/main/java/com/medverify/repository/UserRepository.java
// À AJOUTER

@Query("SELECT u FROM User u " +
       "WHERE u.isActive = true " +
       "AND u.region = :region " +
       "AND u.notificationsEnabled = true")
List<User> findActiveUsersInRegion(@Param("region") String region);
```

#### Backend - Application.properties

```properties
# À AJOUTER - Activer scheduling
spring.task.scheduling.enabled=true
```

**Fichiers à créer/modifier :**

- [ ] `PharmacyDutyNotificationService.java` - À CRÉER
- [ ] `NotificationType.java` - Ajouter PHARMACY_DUTY_UPDATE
- [ ] `UserRepository.java` - Ajouter findActiveUsersInRegion
- [ ] `application.properties` - Activer scheduling

---

### ⭐ 3. SYSTÈME DE NOTATION / AVIS (Priorité BASSE)

**Statut :** ❌ NON IMPLÉMENTÉ (structure BDD prête)

**Problème :**

- Le champ `rating` existe en BDD mais :
  - ❌ Pas d'endpoint POST `/pharmacies/{id}/rate`
  - ❌ Pas d'endpoint POST `/pharmacies/{id}/reviews`
  - ❌ Pas de table `pharmacy_reviews`
  - ❌ Pas d'affichage étoiles moyenne

**Ce qui devrait être fait :**

#### Backend - Migration BDD à créer

```sql
-- V11__pharmacy_reviews_system.sql - À CRÉER

CREATE TABLE pharmacy_reviews (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pharmacy_id UUID NOT NULL REFERENCES pharmacies(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(pharmacy_id, user_id) -- 1 avis par utilisateur par pharmacie
);

CREATE INDEX idx_pharmacy_reviews_pharmacy_id ON pharmacy_reviews(pharmacy_id);
CREATE INDEX idx_pharmacy_reviews_user_id ON pharmacy_reviews(user_id);
```

#### Backend - Entity à créer

```java
// PharmacyReview.java - À CRÉER
@Entity
@Table(name = "pharmacy_reviews")
public class PharmacyReview {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pharmacy_id")
    private Pharmacy pharmacy;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Integer rating; // 1-5
    private String comment;

    // ... getters/setters
}
```

#### Backend - Endpoints à créer

```java
// PharmacyController.java - À AJOUTER

@PostMapping("/{id}/rate")
public ResponseEntity<PharmacyDTO> ratePharmacy(
    @PathVariable UUID id,
    @RequestBody RatePharmacyRequest request,
    @AuthenticationPrincipal User user
) {
    PharmacyDTO pharmacy = pharmacyService.ratePharmacy(id, user.getId(), request.getRating());
    return ResponseEntity.ok(pharmacy);
}

@PostMapping("/{id}/reviews")
public ResponseEntity<ReviewDTO> addReview(
    @PathVariable UUID id,
    @RequestBody AddReviewRequest request,
    @AuthenticationPrincipal User user
) {
    ReviewDTO review = pharmacyService.addReview(id, user.getId(), request);
    return ResponseEntity.ok(review);
}

@GetMapping("/{id}/reviews")
public ResponseEntity<List<ReviewDTO>> getReviews(@PathVariable UUID id) {
    return ResponseEntity.ok(pharmacyService.getReviews(id));
}
```

#### Frontend - Composant StarRating à créer

```typescript
// components/StarRating.tsx - À CRÉER
export const StarRating: React.FC<{
  rating: number;
  onRate?: (rating: number) => void;
}> = ({ rating, onRate }) => {
  return (
    <View style={styles.stars}>
      {[1, 2, 3, 4, 5].map((star) => (
        <TouchableOpacity key={star} onPress={() => onRate?.(star)}>
          <Text style={styles.star}>{star <= rating ? "⭐" : "☆"}</Text>
        </TouchableOpacity>
      ))}
    </View>
  );
};
```

#### Frontend - Écran Avis à créer

```typescript
// PharmacyReviewsScreen.tsx - À CRÉER
// Afficher liste des avis + formulaire pour ajouter avis
```

**Fichiers à créer/modifier :**

- [ ] `V11__pharmacy_reviews_system.sql` - Migration
- [ ] `PharmacyReview.java` - Entity
- [ ] `PharmacyReviewRepository.java` - Repository
- [ ] `PharmacyController.java` - Ajouter 3 endpoints
- [ ] `PharmacyService.java` - Logique notation
- [ ] `StarRating.tsx` - Composant étoiles
- [ ] `PharmacyReviewsScreen.tsx` - Écran avis

---

## 📊 Résumé des fonctionnalités

| Fonctionnalité         | Demandée       | Implémentée | Manquante | Priorité     |
| ---------------------- | -------------- | ----------- | --------- | ------------ |
| Recherche géolocalisée | ✅             | ✅          | -         | -            |
| Filtres avancés        | ✅             | ✅          | -         | -            |
| Recherche par ville    | ✅             | ✅          | -         | -            |
| Itinéraire             | ✅             | ✅          | -         | -            |
| Appel direct           | ✅             | ✅          | -         | -            |
| **Carte interactive**  | ✅             | ❌          | **100%**  | 🔴 **HAUTE** |
| **Notifications push** | ✅             | ❌          | **100%**  | 🟡 MOYENNE   |
| **Système notation**   | ⚠️ (optionnel) | ❌          | **100%**  | 🟢 BASSE     |
| Toggle Liste/Carte     | ⚠️ (implicite) | ❌          | **100%**  | 🔴 **HAUTE** |

---

## 🎯 Plan d'implémentation recommandé

### Phase 1 : Carte interactive (2-3h)

**Impact utilisateur : ÉLEVÉ**

1. Installer dépendances

   ```bash
   npx expo install react-native-maps
   npm install geolib
   ```

2. Configurer Google Maps API

   - Obtenir clé API Google Cloud Console
   - Ajouter dans `app.json`

3. Créer composant MapView

   - Modifier `PharmaciesScreen.tsx`
   - Ajouter markers pharmacies
   - Ajouter InfoWindow sur marker tap

4. Créer toggle Liste/Carte
   - Boutons bascule dans header
   - State `viewMode: 'list' | 'map'`

### Phase 2 : Notifications push (3-4h)

**Impact utilisateur : MOYEN**

1. Créer `PharmacyDutyNotificationService.java`

   - Scheduler vendredi 18h (@Scheduled)
   - Méthode notification urgente

2. Mettre à jour `NotificationType.java`

   - Ajouter `PHARMACY_DUTY_UPDATE`

3. Mettre à jour `UserRepository.java`

   - Query `findActiveUsersInRegion`

4. Activer scheduling

   - `application.properties`

5. Tester notifications
   - Manuellement avec date future
   - Vérifier FCM

### Phase 3 : Système notation (4-5h)

**Impact utilisateur : FAIBLE (nice-to-have)**

1. Migration BDD

   - Table `pharmacy_reviews`
   - Triggers calcul moyenne

2. Backend

   - Entity `PharmacyReview`
   - Repository
   - Endpoints rate/reviews

3. Frontend
   - Composant `StarRating`
   - Écran `PharmacyReviewsScreen`
   - Affichage moyenne sur cartes

---

## 🚀 Commandes d'implémentation rapide

### Carte interactive (PRIORITÉ 1)

```bash
# 1. Installer dépendances
cd MedVerifyApp/MedVerifyExpo
npx expo install react-native-maps
npm install geolib

# 2. Obtenir Google Maps API Key
# https://console.cloud.google.com/
# Activer Maps SDK for Android/iOS

# 3. Ajouter dans app.json
# (voir section Configuration ci-dessus)

# 4. Rebuild
npx expo start --clear
```

### Notifications (PRIORITÉ 2)

```bash
# 1. Créer PharmacyDutyNotificationService.java
# (voir code ci-dessus)

# 2. Activer scheduling
echo "spring.task.scheduling.enabled=true" >> medverify-backend/src/main/resources/application.properties

# 3. Relancer backend
cd medverify-backend
mvn spring-boot:run
```

### Notation (PRIORITÉ 3)

```bash
# 1. Créer migration V11
# (voir SQL ci-dessus)

# 2. Créer entities/services/controllers
# (voir code ci-dessus)

# 3. Relancer backend
mvn spring-boot:run
```

---

## 📋 Checklist de validation finale

### Fonctionnalités de base ✅

- [x] Recherche géolocalisée
- [x] Filtres (toutes/ouvertes/garde/24h)
- [x] Recherche par ville
- [x] Affichage distance
- [x] Itinéraire Google Maps
- [x] Appel direct
- [x] Horaires détaillés
- [x] Services pharmacie

### Fonctionnalités avancées ❌

- [ ] **Carte interactive avec markers**
- [ ] **Toggle vue Liste/Carte**
- [ ] **Notifications push garde weekend**
- [ ] **Notifications changements urgents**
- [ ] **Système notation étoiles**
- [ ] **Avis utilisateurs**
- [ ] **Moyenne étoiles affichée**

---

## 📝 Conclusion

**Implémentation actuelle : 60%**

✅ **Ce qui fonctionne :**

- Backend PostGIS complet et robuste
- Recherche géolocalisée performante
- Interface utilisateur moderne (liste)
- Filtres fonctionnels
- Actions (itinéraire, appel)

❌ **Ce qui manque VRAIMENT :**

1. **Carte interactive** (fonctionnalité clé mentionnée dans le prompt initial)
2. **Notifications push** (structure prévue mais non implémentée)
3. **Système notation** (optionnel mais utile)

🎯 **Recommandation :**
**Implémenter EN PRIORITÉ la carte interactive** car :

- C'était dans les specs initiales ("carte interactive avec markers")
- Impact UX majeur
- Temps d'implémentation raisonnable (2-3h)
- Les dépendances sont documentées mais pas installées

---

**🚀 Prochaine étape suggérée :** Implémenter la carte interactive avec MapView

