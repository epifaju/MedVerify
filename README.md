# 🏥 MedVerify - Application de Vérification d'Authenticité de Médicaments

Application mobile de vérification d'authenticité de médicaments pour lutter contre la contrefaçon pharmaceutique en Guinée-Bissau.

## 📱 Vue d'ensemble

MedVerify est une solution complète permettant aux patients, pharmaciens et autorités sanitaires de vérifier l'authenticité des médicaments en scannant les codes Data Matrix GS1 présents sur les emballages.

### ✨ Fonctionnalités principales

- ✅ **Scan de codes Data Matrix GS1** : Scanner les codes-barres 2D sur les boîtes de médicaments
- ✅ **Vérification d'authenticité instantanée** : Analyse en temps réel avec score de confiance
- ✅ **Mode hors ligne** : Cache local SQLite pour fonctionner sans connexion
- ✅ **Système d'alertes** : Détection de numéros de série dupliqués, lots rappelés, médicaments périmés
- ✅ **Authentification sécurisée** : JWT avec refresh tokens et protection contre les attaques
- ✅ **Multi-rôles** : Patient, Pharmacien, Autorité sanitaire, Admin
- ⏳ **Signalement collaboratif** : (Phase 4 - à venir)
- ⏳ **Dashboard analytics** : (Phase 5 - à venir)

## 🏗️ Architecture

### Stack Technique

**Backend** (Spring Boot 3.2)

- Java 17
- PostgreSQL 14+ avec PostGIS
- Spring Security + JWT
- Flyway pour migrations
- Actuator + Prometheus pour monitoring
- OpenAPI/Swagger pour documentation

**Frontend** (React Native 0.72)

- TypeScript 5.3
- React Navigation 6
- Redux Toolkit pour state management
- Vision Camera pour le scan
- SQLite pour le cache offline
- i18next pour l'internationalisation (FR/PT)

## 📦 Structure du Projet

```
MedVerify/
├── medverify-backend/          # API Spring Boot
│   ├── src/main/java/com/medverify/
│   │   ├── config/             # Configurations (Security, CORS)
│   │   ├── controller/         # REST Controllers
│   │   ├── service/            # Business Logic
│   │   ├── repository/         # Data Access Layer
│   │   ├── entity/             # JPA Entities
│   │   ├── dto/                # Data Transfer Objects
│   │   ├── security/           # JWT & Security
│   │   └── exception/          # Exception Handlers
│   └── src/main/resources/
│       ├── application.yml     # Configuration
│       └── db/migration/       # Migrations Flyway
│
├── MedVerifyApp/               # Application React Native
│   └── src/
│       ├── components/         # Composants réutilisables
│       ├── screens/            # Écrans de l'app
│       ├── navigation/         # Configuration navigation
│       ├── services/           # API calls
│       ├── store/              # Redux state
│       ├── utils/              # Utilitaires (GS1 parser)
│       ├── database/           # SQLite schema
│       ├── locales/            # Traductions (FR/PT)
│       └── types/              # TypeScript types
│
└── PRD_MedVerify.md            # Product Requirements Document
```

## 🚀 Démarrage Rapide

### Prérequis

- **Backend** : JDK 17+, Maven 3.8+, PostgreSQL 14+ avec PostGIS
- **Frontend** : Node.js 16+, npm 8+, React Native CLI
- **Mobile** : Xcode 14+ (iOS) ou Android Studio (Android)

### Installation Backend

```bash
cd medverify-backend

# Créer la base de données
createdb medverify
psql -d medverify -c "CREATE EXTENSION IF NOT EXISTS postgis;"

# Copier et configurer les variables d'environnement
cp .env.example .env

# Build et lancer
mvn clean package
mvn spring-boot:run
```

L'API sera disponible sur `http://localhost:8080`

**Documentation API** : http://localhost:8080/swagger-ui.html

### Installation Frontend

```bash
cd MedVerifyApp

# Installer les dépendances
npm install

# iOS - installer les pods
cd ios && pod install && cd ..

# Lancer sur Android
npm run android

# Lancer sur iOS
npm run ios
```

## 🔐 Authentification

### Endpoints API

```
POST /api/v1/auth/register    - Inscription utilisateur
POST /api/v1/auth/login       - Connexion
POST /api/v1/auth/refresh     - Rafraîchir le token
```

### Utilisateur par défaut

```
Email: admin@medverify.gw
Password: Admin@123456
Role: ADMIN
```

## 💊 Vérification de Médicaments

### Endpoints API

```
POST /api/v1/medications/verify        - Vérifier un médicament
GET  /api/v1/medications/{id}          - Détails d'un médicament
GET  /api/v1/medications/search?name=  - Rechercher par nom
GET  /api/v1/medications/essential     - Médicaments essentiels OMS
```

### Format GS1 Data Matrix

```
(01)03401234567890(17)251231(10)ABC123(21)XYZ789

Décodage :
- (01) : GTIN - 14 chiffres
- (17) : Date péremption (AAMMJJ)
- (10) : Numéro de lot
- (21) : Numéro de série unique
```

### Règles de Vérification

L'algorithme d'authenticité analyse plusieurs critères :

1. **GTIN actif** : Le code GTIN doit être enregistré et actif
2. **Numéro de série dupliqué** : Si scanné > 5 fois → Suspect (confidence -0.6)
3. **Médicament périmé** : Date d'expiration dépassée → Alert (confidence -0.3)
4. **Lot rappelé** : Lot avec recall actif → Critique (confidence -0.8)

**Statut final** :

- `AUTHENTIC` : confidence ≥ 0.7
- `SUSPICIOUS` : confidence < 0.7
- `UNKNOWN` : GTIN non trouvé

## 📊 Base de Données

### Migrations Flyway

- `V1__initial_schema.sql` : Users, Refresh Tokens
- `V2__medications_schema.sql` : Medications, Batches, Scan History

### Médicaments de Test

10 médicaments essentiels OMS pré-chargés :

- Paracétamol 500mg
- Amoxicilline 500mg
- Ibuprofène 400mg
- Metformine 850mg
- Oméprazole 20mg
- Amlodipine 5mg
- Atorvastatine 20mg
- Salbutamol 100μg
- Losartan 50mg
- Ciprofloxacine 500mg

## 🧪 Tests

### Backend

```bash
# Lancer tous les tests
mvn test

# Avec coverage
mvn clean test jacoco:report
```

### Frontend

```bash
# Tests unitaires
npm test

# Avec coverage
npm run test:coverage
```

## 📈 Monitoring

### Actuator Endpoints

- `http://localhost:8080/actuator/health` - Health check
- `http://localhost:8080/actuator/metrics` - Métriques
- `http://localhost:8080/actuator/prometheus` - Métriques Prometheus

### Métriques Custom

- `scan.verification` : Compteur de scans par statut
- `scan.verification.duration` : Temps de vérification

## 🌍 Internationalisation

L'application supporte :

- 🇫🇷 Français
- 🇵🇹 Portugais

Fichiers de traduction : `MedVerifyApp/src/locales/`

## 🔒 Sécurité

- ✅ Authentification JWT avec refresh tokens
- ✅ Protection CSRF désactivée (API stateless)
- ✅ CORS configuré
- ✅ Blocage de compte après 5 tentatives échouées (1h)
- ✅ Hash de mots de passe avec BCrypt (strength 12)
- ✅ Validation des entrées avec Jakarta Validation
- ✅ Rate limiting (via Spring Security)

## 📝 État d'Avancement

### ✅ Phases Complétées

- [x] **Phase 1** : Setup initial Backend + Frontend
- [x] **Phase 2** : Authentification & Sécurité
  - Backend : User entities, JWT, Security config
  - Frontend : Login/Register screens, Redux auth
- [x] **Phase 3** : Scan & Vérification Médicaments
  - Backend : Medication entities, Verification service
  - Frontend : Scanner Data Matrix, Écrans résultats, Cache offline

### ⏳ Phases Restantes

- [ ] **Phase 4** : Signalement de Médicaments Suspects
  - Backend : Report entity, notification service
  - Frontend : Formulaire de signalement
- [ ] **Phase 5** : Dashboard Autorités Sanitaires

  - Backend : Analytics service, requêtes statistiques
  - Frontend : Dashboard avec charts, KPIs

- [ ] **Phase 6** : Tests E2E & Optimisations
  - Tests Detox
  - Indexes DB supplémentaires
  - Cache Redis

## 🎯 Prochaines Étapes

1. **Phase 4** : Implémenter le système de signalement
2. **Phase 5** : Créer le dashboard pour les autorités
3. **Déploiement** :
   - Backend sur AWS/Heroku
   - Base de données PostgreSQL managée
   - Application sur App Store & Google Play

## 📄 Licence

MIT

## 👥 Contact

Pour toute question ou suggestion : support@medverify.gw

---

**Note** : Cette application est un outil de lutte contre la contrefaçon pharmaceutique. Elle ne remplace pas l'expertise médicale professionnelle.

