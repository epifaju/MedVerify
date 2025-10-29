# ⚡ Quick Start - Pharmacies

## 🚀 Lancement rapide (5 minutes)

### 1️⃣ Backend

```bash
# Activer PostGIS
psql -U postgres -d medverify -c "CREATE EXTENSION IF NOT EXISTS postgis;"

# Lancer le backend (migration auto)
cd medverify-backend
./mvnw spring-boot:run
```

### 2️⃣ Frontend

```bash
cd MedVerifyApp/MedVerifyExpo

# Installer dépendances
npx expo install react-native-maps
npm install geolib

# Configurer Google Maps API Key dans app.json
# (voir GUIDE_COMPLET_PHARMACIES.md section 4)

# Lancer l'app
npx expo start --android
# ou
npx expo start --ios
```

### 3️⃣ Tester

**Backend (Postman/Insomnia) :**

```http
POST http://localhost:8080/api/v1/pharmacies/search
Content-Type: application/json

{
  "latitude": 11.8636,
  "longitude": -15.5984,
  "radiusKm": 5
}
```

**Frontend :**

1. Ouvrir l'app
2. Aller sur onglet "Pharmacies" 🏥
3. Autoriser la géolocalisation
4. Voir les 3 pharmacies de test à Bissau

## 📋 Endpoints principaux

- `POST /api/v1/pharmacies/search` - Recherche géolocalisée
- `GET /api/v1/pharmacies/{id}` - Détails pharmacie
- `GET /api/v1/pharmacies/on-duty` - Pharmacies de garde
- `GET /api/v1/pharmacies/search-by-city?city=Bissau` - Recherche par ville

## ✅ Vérification rapide

```sql
-- Vérifier données
SELECT name, city, ST_AsText(location) FROM pharmacies;

-- Tester distance PostGIS
SELECT
    name,
    ST_Distance(
        location::geography,
        ST_SetSRID(ST_MakePoint(-15.5984, 11.8636), 4326)::geography
    ) / 1000 as distance_km
FROM pharmacies
ORDER BY distance_km;
```

## 🐛 Problèmes courants

**PostGIS manquant :** `CREATE EXTENSION postgis;`

**Permission localisation :** Paramètres téléphone > Autorisations > Position

**Google Maps vide :** Ajouter API Key dans `app.json`

---

**📚 Guide complet :** Voir `GUIDE_COMPLET_PHARMACIES.md`

