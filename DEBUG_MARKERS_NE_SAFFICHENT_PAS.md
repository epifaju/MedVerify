# 🔍 Debug : Markers Pharmacies Ne S'affichent Pas Sur La Carte

**Date :** 14 octobre 2025  
**Problème :** La carte affiche la position de l'utilisateur (cercle bleu) mais pas les 3 markers des pharmacies

---

## 🐛 Diagnostic approfondi

### Ce qui fonctionne ✅

- Backend démarre sans erreur
- Position utilisateur affichée (cercle bleu)
- Carte OpenStreetMap chargée
- WebView prête et reçoit les messages

### Ce qui ne fonctionne PAS ❌

- Les 3 markers des pharmacies ne s'affichent pas sur la carte

---

## 🔍 Hypothèses principales

### Hypothèse 1 : Les coordonnées sont inversées (latitude/longitude)

**PostGIS stocke :** `Point(longitude, latitude)` → `Point(X, Y)`  
**Leaflet attend :** `[latitude, longitude]` → `[Y, X]`

**Vérification :**

- Backend extrait correctement : `latitude = coord.getY()`, `longitude = coord.getX()` ✅
- Les données de test utilisent : `ST_MakePoint(-15.5984, 11.8636)`
  - `-15.5984` = longitude (X)
  - `11.8636` = latitude (Y)

### Hypothèse 2 : Le backend ne retourne pas les pharmacies

**Vérification nécessaire :** Tester l'API via Swagger ou curl.

### Hypothèse 3 : Les coordonnées sont correctes mais le rayon de recherche est trop petit

**Données de test :**

```
Pharmacie Centrale: lon=-15.5984, lat=11.8636
Pharmacie du Port:  lon=-15.5970, lat=11.8650
Pharmacie de Nuit:  lon=-15.5995, lat=11.8620
```

**Si l'utilisateur est à :** `lat=11.8636, lon=-15.5984` (Bissau)  
**Rayon de recherche :** 5km

Les 3 pharmacies devraient être trouvées (distance < 5km).

---

## 🧪 Tests de diagnostic

### TEST 1 : Vérifier que le backend retourne bien les pharmacies

**Via Swagger UI :**

```
URL: http://localhost:8080/swagger-ui.html
Endpoint: POST /api/v1/pharmacies/search
```

**Body de test :**

```json
{
  "latitude": 11.8636,
  "longitude": -15.5984,
  "radiusKm": 5,
  "limit": 10
}
```

**Résultat attendu :**

```json
[
  {
    "id": "...",
    "name": "Pharmacie Centrale de Bissau",
    "address": "Avenue Amilcar Cabral, Centre-ville",
    "city": "Bissau",
    "latitude": 11.8636,
    "longitude": -15.5984,
    "distanceKm": 0.0,
    "is24h": true,
    ...
  },
  {
    "id": "...",
    "name": "Pharmacie du Port",
    ...
    "latitude": 11.8650,
    "longitude": -15.5970,
    ...
  },
  {
    "id": "...",
    "name": "Pharmacie de Nuit Bissau",
    ...
    "latitude": 11.8620,
    "longitude": -15.5995,
    ...
  }
]
```

**Si le backend retourne 0 pharmacies :**
→ Vérifier que la migration V10 est bien appliquée :

```sql
SELECT id, name,
       ST_X(location::geometry) as longitude,
       ST_Y(location::geometry) as latitude
FROM pharmacies
WHERE city = 'Bissau' AND is_active = TRUE;
```

**Doit retourner :**

```
| name                           | longitude  | latitude |
|--------------------------------|------------|----------|
| Pharmacie Centrale de Bissau   | -15.5984   | 11.8636  |
| Pharmacie du Port              | -15.5970   | 11.8650  |
| Pharmacie de Nuit Bissau       | -15.5995   | 11.8620  |
```

### TEST 2 : Vérifier les logs backend

**Logs ajoutés dans PharmacyService :**

```
🔍 Recherche pharmacies - User position: lat=11.8636, lng=-15.5984, radius=5.0km
✅ Pharmacies trouvées: 3
   🏥 Pharmacie Centrale de Bissau - lat=11.8636, lng=-15.5984, distance=0.00km
   🏥 Pharmacie du Port - lat=11.8650, lng=-15.5970, distance=0.XX km
   🏥 Pharmacie de Nuit Bissau - lat=11.8620, lng=-15.5995, distance=0.XX km
```

**Pour voir les logs :**

```powershell
# Dans le terminal où tourne le backend
# Chercher les logs avec les emojis 🔍 et 🏥
```

### TEST 3 : Vérifier les données reçues par le frontend

**Logs React Native (dans PharmaciesScreen) :**

```
🔍 Initialisation recherche pharmacies...
✅ Résultats recherche: {
  count: 3,
  pharmacies: [
    { id: '...', name: 'Pharmacie Centrale de Bissau', lat: 11.8636, lng: -15.5984 },
    { id: '...', name: 'Pharmacie du Port', lat: 11.8650, lng: -15.5970 },
    { id: '...', name: 'Pharmacie de Nuit Bissau', lat: 11.8620, lng: -15.5995 }
  ]
}
```

**Si count = 0 :** Le problème vient du backend ou de l'API.  
**Si count = 3 :** Le problème vient de l'affichage dans Leaflet.

### TEST 4 : Vérifier les données envoyées à la WebView

**Logs React Native (dans LeafletMapView) :**

```
📤 Message envoyé à WebView: {
  pharmaciesCount: 3,
  userLocation: { latitude: 11.8636, longitude: -15.5984 },
  pharmacies: [
    { id: '...', name: 'Pharmacie Centrale de Bissau', lat: 11.8636, lng: -15.5984 },
    { id: '...', name: 'Pharmacie du Port', lat: 11.8650, lng: -15.5970 },
    { id: '...', name: 'Pharmacie de Nuit Bissau', lat: 11.8620, lng: -15.5995 }
  ]
}
```

**Si pharmaciesCount = 0 :** Le hook usePharmacies ne retourne pas les données.  
**Si pharmaciesCount = 3 :** Les données sont bien envoyées.

### TEST 5 : Vérifier les données reçues par Leaflet dans la WebView

**Logs JavaScript (dans la WebView) :**

```
📥 Message reçu (window.addEventListener): UPDATE_DATA
📊 Données: { pharmaciesCount: 3, hasUserLocation: true }
🗺️ updateMap appelé { pharmaciesCount: 3, hasUserLocation: true }
📍 Marker utilisateur ajouté: [11.8636, -15.5984]
🏥 Ajout marker pharmacie: { id: '...', name: 'Pharmacie Centrale de Bissau', lat: 11.8636, lng: -15.5984 }
🏥 Ajout marker pharmacie: { id: '...', name: 'Pharmacie du Port', lat: 11.8650, lng: -15.5970 }
🏥 Ajout marker pharmacie: { id: '...', name: 'Pharmacie de Nuit Bissau', lat: 11.8620, lng: -15.5995 }
✅ Tous les markers ajoutés: 3
```

**Si "🏥 Ajout marker" n'apparaît pas :** Le problème vient du JavaScript dans la WebView.

---

## 🔧 Solutions possibles

### Solution 1 : Si le backend ne retourne rien

**Vérifier la migration V10 :**

```sql
-- Voir si les pharmacies existent
SELECT COUNT(*) FROM pharmacies WHERE city = 'Bissau';
-- Doit retourner 3

-- Voir les coordonnées
SELECT name,
       ST_AsText(location) as location_wkt,
       ST_X(location::geometry) as longitude,
       ST_Y(location::geometry) as latitude
FROM pharmacies
WHERE city = 'Bissau';
```

**Si COUNT = 0 :** Re-run la migration V10.

### Solution 2 : Si les coordonnées sont à (0,0) ou nulles

**Problème :** La géométrie PostGIS n'a pas été créée correctement.

**Fix :** Mettre à jour manuellement :

```sql
UPDATE pharmacies SET location = ST_SetSRID(ST_MakePoint(-15.5984, 11.8636), 4326)::geography
WHERE name = 'Pharmacie Centrale de Bissau';

UPDATE pharmacies SET location = ST_SetSRID(ST_MakePoint(-15.5970, 11.8650), 4326)::geography
WHERE name = 'Pharmacie du Port';

UPDATE pharmacies SET location = ST_SetSRID(ST_MakePoint(-15.5995, 11.8620), 4326)::geography
WHERE name = 'Pharmacie de Nuit Bissau';
```

### Solution 3 : Si Leaflet reçoit les données mais n'affiche pas les markers

**Problème :** L'ordre des coordonnées est inversé dans Leaflet.

**Vérifier dans LeafletMapView.tsx (ligne ~190) :**

```javascript
const marker = L.marker([pharmacy.latitude, pharmacy.longitude], {  // ✅ CORRECT
```

**Si c'est :**

```javascript
const marker = L.marker([pharmacy.longitude, pharmacy.latitude], {  // ❌ INCORRECT
```

**Alors inverser :**

```javascript
const marker = L.marker([pharmacy.latitude, pharmacy.longitude], {
```

### Solution 4 : Si les markers sont créés mais hors de la vue

**Problème :** La carte est centrée sur l'utilisateur mais les markers sont loin.

**Debug :** Ajouter temporairement dans `updateMap` :

```javascript
console.log("🗺️ Bounds de la carte:", map.getBounds());
pharmacies.forEach((pharmacy) => {
  console.log("📍 Pharmacie:", pharmacy.name, [
    pharmacy.latitude,
    pharmacy.longitude,
  ]);
});
```

**Fix :** Ajuster le zoom ou centrer sur les markers :

```javascript
if (pharmacies.length > 0) {
  const bounds = L.latLngBounds(
    pharmacies.map((p) => [p.latitude, p.longitude])
  );
  if (userLocation) {
    bounds.extend([userLocation.latitude, userLocation.longitude]);
  }
  map.fitBounds(bounds, { padding: [50, 50] });
}
```

---

## 📝 Checklist complète

### Backend ✅

- [ ] Backend démarre sans erreur
- [ ] Migration V10 appliquée
- [ ] 3 pharmacies dans la table `pharmacies`
- [ ] Coordonnées correctes (longitude negative, latitude positive pour Bissau)
- [ ] Requête SQL `findPharmaciesWithinRadius` retourne 3 résultats
- [ ] Logs backend affichent "✅ Pharmacies trouvées: 3"

### Frontend ✅

- [ ] App Expo démarrée sans erreur
- [ ] Permission géolocalisation accordée
- [ ] Position GPS simulée (Bissau: 11.8636, -15.5984)
- [ ] Hook `usePharmacies` retourne 3 pharmacies
- [ ] Logs React Native affichent "✅ Résultats recherche: { count: 3 }"
- [ ] WebView reçoit le message avec 3 pharmacies
- [ ] Logs WebView affichent "🏥 Ajout marker pharmacie" × 3
- [ ] Logs WebView affichent "✅ Tous les markers ajoutés: 3"

### Carte ✅

- [ ] Carte OpenStreetMap affichée
- [ ] Cercle bleu (position utilisateur) visible
- [ ] 3 markers pharmacies visibles
- [ ] Popups s'affichent au clic
- [ ] Navigation vers détails fonctionne

---

## 🚀 Prochaine étape

**Lancez le backend avec les nouveaux logs, puis testez l'app et partagez-moi :**

1. **Les logs backend** (avec 🔍 et 🏥)
2. **Les logs React Native** (avec 🔍, ✅, 📤)
3. **Les logs WebView** (avec 📥, 🏥, ✅)
4. **Une capture d'écran** de la carte

**Cela me permettra de localiser exactement où le problème se situe !** 🔍

