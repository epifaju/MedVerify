# 🏥 Guide Complet - Fonctionnalité Pharmacies avec Géolocalisation

## 📋 Vue d'ensemble

Cette fonctionnalité permet aux citoyens de trouver facilement :

- ✅ Pharmacies ouvertes autour de leur position
- ✅ Pharmacies de garde (weekend, jours fériés)
- ✅ Pharmacies de nuit (ouvertes 24h ou jusqu'à minuit)
- ✅ Géolocalisation avec recherche par rayon
- ✅ Recherche par ville/quartier
- ✅ Itinéraire vers pharmacie sélectionnée
- ✅ Appel direct depuis l'application

**Stack technique :**

- **Backend** : Spring Boot + PostgreSQL + PostGIS
- **Frontend** : React Native Expo + expo-location
- **Géolocalisation** : PostGIS (requêtes géospatiales natives)

---

## 🚀 Installation et Configuration

### 1️⃣ Backend - Installation PostGIS

**PostgreSQL doit avoir l'extension PostGIS activée :**

```sql
-- Se connecter à la base de données
psql -U postgres -d medverify

-- Activer PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

-- Vérifier installation
SELECT PostGIS_version();
```

### 2️⃣ Backend - Lancer la migration

La migration Flyway `V10__pharmacies_system.sql` sera exécutée automatiquement au démarrage du backend.

```bash
cd medverify-backend
./mvnw spring-boot:run
```

**Vérification :**

```sql
-- Vérifier tables créées
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public'
AND table_name IN ('pharmacies', 'on_duty_schedules', 'pharmacy_services');

-- Vérifier données de test
SELECT name, city, ST_AsText(location) FROM pharmacies;
```

### 3️⃣ Frontend - Installation des dépendances

```bash
cd MedVerifyApp/MedVerifyExpo

# Installer react-native-maps
npx expo install react-native-maps

# Installer geolib (calculs de distance)
npm install geolib

# Installer types
npm install --save-dev @types/geolib
```

### 4️⃣ Frontend - Configuration Expo (app.json)

**Important** : Vous devez obtenir une clé API Google Maps.

1. Aller sur [Google Cloud Console](https://console.cloud.google.com/)
2. Créer un projet
3. Activer "Maps SDK for Android" et "Maps SDK for iOS"
4. Créer une clé API

**Mettre à jour `app.json` :**

```json
{
  "expo": {
    "plugins": [
      [
        "expo-location",
        {
          "locationAlwaysAndWhenInUsePermission": "Nous utilisons votre position pour trouver les pharmacies proches."
        }
      ]
    ],
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

---

## 📁 Architecture des fichiers créés

### Backend

```
medverify-backend/
├── src/main/java/com/medverify/
│   ├── entity/
│   │   ├── Pharmacy.java                    ✅ Entité pharmacie avec PostGIS
│   │   ├── OnDutySchedule.java              ✅ Planning de garde
│   │   ├── OpeningHours.java                ✅ Horaires hebdomadaires
│   │   ├── OpeningHoursDay.java             ✅ Horaires journaliers
│   │   └── DutyType.java                    ✅ Enum types de garde
│   ├── repository/
│   │   ├── PharmacyRepository.java          ✅ Requêtes PostGIS
│   │   └── OnDutyScheduleRepository.java    ✅ Requêtes garde
│   ├── service/
│   │   └── PharmacyService.java             ✅ Logique métier
│   ├── controller/
│   │   └── PharmacyController.java          ✅ Endpoints REST
│   └── dto/
│       ├── response/
│       │   └── PharmacyDTO.java             ✅ DTO réponse
│       └── request/
│           └── PharmacySearchRequest.java   ✅ DTO recherche
└── src/main/resources/db/migration/
    └── V10__pharmacies_system.sql           ✅ Migration Flyway
```

### Frontend

```
MedVerifyApp/MedVerifyExpo/src/
├── screens/
│   └── Pharmacies/
│       ├── PharmaciesScreen.tsx             ✅ Liste pharmacies + filtres
│       └── PharmacyDetailScreen.tsx         ✅ Détails + horaires
├── services/
│   ├── GeolocationService.ts                ✅ Géolocalisation expo-location
│   └── PharmacyService.ts                   ✅ Appels API pharmacies
├── hooks/
│   ├── usePharmacies.ts                     ✅ Hook recherche pharmacies
│   └── usePharmacyDetails.ts                ✅ Hook détails pharmacie
├── types/
│   └── pharmacy.types.ts                    ✅ Types TypeScript
└── navigation/
    └── PharmaciesNavigator.tsx              ✅ Stack Navigator
```

---

## 🔌 API Endpoints

### 1. Recherche de pharmacies

**POST** `/api/v1/pharmacies/search`

```json
{
  "latitude": 11.8636,
  "longitude": -15.5984,
  "radiusKm": 5,
  "limit": 20,
  "openNow": true,
  "onDutyOnly": false,
  "is24h": false,
  "requiredServices": ["DELIVERY"]
}
```

**Réponse :**

```json
[
  {
    "id": "uuid",
    "name": "Pharmacie Centrale de Bissau",
    "address": "Avenue Amilcar Cabral",
    "city": "Bissau",
    "region": "Bissau",
    "latitude": 11.8636,
    "longitude": -15.5984,
    "distanceKm": 0.5,
    "is24h": true,
    "isOpenNow": true,
    "isOnDuty": false,
    "phoneNumber": "+245 955 123 456",
    "services": ["DELIVERY", "COVID_TEST"]
  }
]
```

### 2. Détails d'une pharmacie

**GET** `/api/v1/pharmacies/{id}`

### 3. Pharmacies de garde

**GET** `/api/v1/pharmacies/on-duty?latitude=11.8636&longitude=-15.5984&radiusKm=10`

### 4. Recherche par ville

**GET** `/api/v1/pharmacies/search-by-city?city=Bissau`

### 5. Pharmacies 24h

**GET** `/api/v1/pharmacies/24h`

---

## 🗺️ Fonctionnalités PostGIS

### Requêtes géospatiales implémentées

**1. Pharmacies dans un rayon (ST_DWithin)**

```sql
SELECT p.*,
       ST_Distance(p.location::geography,
                   ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) as distance
FROM pharmacies p
WHERE ST_DWithin(
    p.location::geography,
    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
    :radiusMeters
)
ORDER BY distance
```

**2. N pharmacies les plus proches (opérateur <->)**

```sql
SELECT p.*,
       ST_Distance(p.location::geography, ...) as distance
FROM pharmacies p
ORDER BY p.location <-> ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geometry
LIMIT :limit
```

**3. Pharmacies de garde dans un rayon (JOIN + ST_DWithin)**

```sql
SELECT p.*, s.duty_type, s.start_date, s.end_date
FROM on_duty_schedules s
JOIN pharmacies p ON s.pharmacy_id = p.id
WHERE :date BETWEEN s.start_date AND s.end_date
AND ST_DWithin(p.location::geography, ...)
```

---

## 📱 Utilisation Frontend

### Exemples de code

**1. Rechercher pharmacies autour de l'utilisateur**

```typescript
import { usePharmacies } from "../hooks/usePharmacies";

const MyComponent = () => {
  const { searchNearby, pharmacies, loading } = usePharmacies();

  useEffect(() => {
    searchNearby(5); // Rayon 5km
  }, []);

  return (
    <FlatList
      data={pharmacies}
      renderItem={({ item }) => <PharmacyCard pharmacy={item} />}
    />
  );
};
```

**2. Rechercher pharmacies ouvertes**

```typescript
const { searchOpenNow } = usePharmacies();

await searchOpenNow(5); // Pharmacies ouvertes dans 5km
```

**3. Rechercher pharmacies de garde**

```typescript
const { searchOnDuty } = usePharmacies();

await searchOnDuty(10); // Pharmacies de garde dans 10km
```

**4. Ouvrir itinéraire**

```typescript
import PharmacyService from "../services/PharmacyService";

PharmacyService.openDirections(pharmacy); // Ouvre Google Maps/Apple Maps
```

**5. Appeler une pharmacie**

```typescript
PharmacyService.callPharmacy(pharmacy.phoneNumber); // Ouvre le dialer
```

---

## 🧪 Tests

### Backend - Tests Postman/Insomnia

**1. Rechercher pharmacies à Bissau (rayon 5km)**

```http
POST http://localhost:8080/api/v1/pharmacies/search
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN

{
  "latitude": 11.8636,
  "longitude": -15.5984,
  "radiusKm": 5,
  "limit": 20
}
```

**2. Pharmacies ouvertes maintenant**

```http
POST http://localhost:8080/api/v1/pharmacies/search
Content-Type: application/json

{
  "latitude": 11.8636,
  "longitude": -15.5984,
  "radiusKm": 5,
  "openNow": true
}
```

**3. Pharmacies de garde**

```http
GET http://localhost:8080/api/v1/pharmacies/on-duty?latitude=11.8636&longitude=-15.5984&radiusKm=10
Authorization: Bearer YOUR_TOKEN
```

### Frontend - Test sur émulateur

**Android :**

```bash
cd MedVerifyApp/MedVerifyExpo
npx expo start --android
```

**iOS :**

```bash
npx expo start --ios
```

**Simuler position GPS :**

- **Android Studio** : Extended Controls > Location > Bissau (11.8636, -15.5984)
- **Xcode** : Features > Location > Custom Location

---

## 🔧 Configuration des horaires

### Format JSON pour opening_hours

```json
{
  "monday": {
    "closed": false,
    "morningOpen": "08:00",
    "morningClose": "13:00",
    "afternoonOpen": "15:00",
    "afternoonClose": "19:00"
  },
  "tuesday": { ... },
  "saturday": {
    "closed": false,
    "morningOpen": "09:00",
    "morningClose": "13:00"
  },
  "sunday": {
    "closed": true
  }
}
```

### Pharmacie de nuit (horaires traversant minuit)

```json
{
  "monday": {
    "morningOpen": "08:00",
    "morningClose": "13:00",
    "afternoonOpen": "15:00",
    "afternoonClose": "19:00",
    "eveningOpen": "20:00",
    "eveningClose": "02:00" // 2h du matin (lendemain)
  }
}
```

---

## 📊 Données de test

La migration inclut 3 pharmacies de test à Bissau :

1. **Pharmacie Centrale de Bissau** (24h/24)

   - Coordonnées : 11.8636, -15.5984
   - Services : Livraison, Visite domicile, Test COVID

2. **Pharmacie du Port** (horaires normaux)

   - Coordonnées : 11.8650, -15.5970
   - Horaires : Lun-Ven 8h-13h, 15h-19h / Sam 9h-13h

3. **Pharmacie de Nuit Bissau** (nuit)
   - Coordonnées : 11.8620, -15.5995
   - Horaires : jusqu'à 23h59 + soir

### Ajouter une pharmacie via SQL

```sql
INSERT INTO pharmacies (
    name, address, city, region, country, location,
    phone_number, is_24h, is_verified, is_active
)
VALUES (
    'Ma Pharmacie',
    'Adresse complète',
    'Bissau',
    'Bissau',
    'Guinée-Bissau',
    ST_SetSRID(ST_MakePoint(-15.5984, 11.8636), 4326)::geography,
    '+245 955 000 000',
    FALSE,
    TRUE,
    TRUE
);
```

### Ajouter une garde

```sql
INSERT INTO on_duty_schedules (
    pharmacy_id, start_date, end_date, duty_type, region
)
SELECT
    id,
    '2025-10-18',
    '2025-10-20',
    'WEEKEND'::duty_type,
    'Bissau'
FROM pharmacies
WHERE name = 'Ma Pharmacie';
```

---

## 🐛 Dépannage

### Erreur : PostGIS extension not found

```sql
-- Se connecter en tant que superuser
psql -U postgres

-- Activer PostGIS
CREATE EXTENSION postgis;
```

### Erreur : Location permission denied (Frontend)

**Solution Android :**

- Paramètres > Applications > MedVerify > Autorisations > Position > Autoriser

**Solution iOS :**

- Réglages > Confidentialité > Localisation > MedVerify > Toujours autoriser

### Erreur : Google Maps ne s'affiche pas

1. Vérifier que la clé API est dans `app.json`
2. Vérifier que Maps SDK for Android/iOS est activé sur Google Cloud
3. Rebuild l'application : `npx expo start --clear`

### Aucune pharmacie trouvée

1. Vérifier que les données de test sont insérées :

   ```sql
   SELECT COUNT(*) FROM pharmacies WHERE is_active = true;
   ```

2. Vérifier que PostGIS calcule bien les distances :
   ```sql
   SELECT
       name,
       ST_Distance(
           location::geography,
           ST_SetSRID(ST_MakePoint(-15.5984, 11.8636), 4326)::geography
       ) / 1000 as distance_km
   FROM pharmacies
   ORDER BY distance_km;
   ```

---

## 🚀 Prochaines étapes (optionnel)

### 1. Notifications push pour changements de garde

**Fichier déjà créé** : `PharmacyDutyNotificationService.java`

Activer le scheduler :

```java
@Scheduled(cron = "0 0 18 * * FRI") // Vendredi 18h
public void notifyWeekendDutyPharmacies() {
    // Envoie notifications aux utilisateurs
}
```

### 2. Carte interactive avec MapView

**Installer react-native-maps** (déjà dans instructions)

**Ajouter dans PharmaciesScreen** :

```typescript
import MapView, { Marker } from "react-native-maps";

<MapView
  initialRegion={{
    latitude: userLocation.latitude,
    longitude: userLocation.longitude,
    latitudeDelta: 0.05,
    longitudeDelta: 0.05,
  }}
>
  {pharmacies.map((pharmacy) => (
    <Marker
      key={pharmacy.id}
      coordinate={{
        latitude: pharmacy.latitude,
        longitude: pharmacy.longitude,
      }}
      title={pharmacy.name}
    />
  ))}
</MapView>;
```

### 3. Système de notation

Ajouter endpoints pour :

- Noter une pharmacie (POST `/api/v1/pharmacies/{id}/rate`)
- Ajouter avis (POST `/api/v1/pharmacies/{id}/reviews`)

---

## ✅ Checklist de validation

### Backend

- [ ] Extension PostGIS activée
- [ ] Migration V10 exécutée avec succès
- [ ] 3 pharmacies de test présentes
- [ ] Endpoint `/api/v1/pharmacies/search` retourne résultats
- [ ] Distances calculées correctement (PostGIS)
- [ ] Endpoint `/api/v1/pharmacies/on-duty` fonctionne

### Frontend

- [ ] Dépendances installées (react-native-maps, expo-location)
- [ ] Google Maps API Key configurée
- [ ] Permission localisation demandée
- [ ] Onglet "Pharmacies" visible dans bottom tabs
- [ ] Liste des pharmacies s'affiche
- [ ] Filtres (ouvert, garde, 24h) fonctionnent
- [ ] Navigation vers détails fonctionne
- [ ] Bouton "Itinéraire" ouvre Google Maps
- [ ] Bouton "Appeler" ouvre le dialer
- [ ] Horaires s'affichent correctement

---

## 📞 Support

**Problème avec PostGIS ?**

- Documentation : https://postgis.net/documentation/
- Forum : https://gis.stackexchange.com/

**Problème avec expo-location ?**

- Documentation : https://docs.expo.dev/versions/latest/sdk/location/

**Problème avec react-native-maps ?**

- Documentation : https://github.com/react-native-maps/react-native-maps

---

## 🎉 Félicitations !

Vous avez maintenant une fonctionnalité complète de localisation de pharmacies avec :

- ✅ Géolocalisation temps réel
- ✅ Recherche par rayon PostGIS
- ✅ Filtres avancés (ouvert, garde, 24h)
- ✅ Itinéraire Google Maps/Apple Maps
- ✅ Appel direct
- ✅ Affichage horaires

**La fonctionnalité est prête à être testée et déployée ! 🚀**
