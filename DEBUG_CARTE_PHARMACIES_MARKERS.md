# 🔍 Debug : Markers Pharmacies Non Visibles sur la Carte

**Date :** 14 octobre 2025  
**Problème :** Les 3 markers des pharmacies de test à Bissau ne sont pas visibles sur la carte interactive

---

## 🐛 Diagnostic du problème

### Points à vérifier :

1. ✅ **Backend retourne bien les pharmacies ?**
2. ✅ **Frontend reçoit bien les données ?**
3. ❓ **WebView reçoit bien les messages ?**
4. ❓ **Leaflet affiche bien les markers ?**

---

## 🔎 Vérifications étape par étape

### ÉTAPE 1 : Backend retourne les pharmacies ✅

**Données de test dans V10\_\_pharmacies_system.sql :**

```sql
-- Pharmacie 1: Pharmacie Centrale de Bissau
-- Coordonnées: -15.5984, 11.8636
-- is_24h: TRUE

-- Pharmacie 2: Pharmacie du Port
-- Coordonnées: -15.5970, 11.8650
-- is_24h: FALSE

-- Pharmacie 3: Pharmacie de Nuit Bissau
-- Coordonnées: -15.5995, 11.8620
-- is_night_pharmacy: TRUE
```

**Requête PostGIS utilisée :**

```sql
SELECT p.*, ST_Distance(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geography) / 1000.0 AS distance_km
FROM pharmacies p
WHERE p.is_active = TRUE
  AND ST_DWithin(p.location, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geography, :radiusKm * 1000)
ORDER BY distance_km ASC
LIMIT :limit
```

### ÉTAPE 2 : Frontend reçoit les données ✅

**Hook usePharmacies :**

```typescript
const searchNearby = async (
  radiusKm: number = 5,
  filters?: Partial<PharmacySearchParams>
): Promise<Pharmacy[]> => {
  const coords = userLocation || (await getUserLocation());

  if (!coords) {
    setError("Position non disponible");
    return [];
  }

  return searchPharmacies({
    latitude: coords.latitude,
    longitude: coords.longitude,
    radiusKm,
    ...filters,
  });
};
```

### ÉTAPE 3 : WebView reçoit les messages ❓

**Code dans LeafletMapView.tsx (lignes 34-43) :**

```typescript
useEffect(() => {
  if (webViewRef.current && userLocation) {
    const message = JSON.stringify({
      type: "UPDATE_DATA",
      pharmacies,
      userLocation,
    });
    webViewRef.current.postMessage(message);
  }
}, [pharmacies, userLocation]);
```

**⚠️ PROBLÈME POTENTIEL #1 :** Le `useEffect` ne s'exécute que si `userLocation` est défini ET que `pharmacies` change. Si `pharmacies` est un tableau vide initialement, le message ne sera pas envoyé.

### ÉTAPE 4 : Leaflet affiche les markers ❓

**Code JavaScript dans la WebView (lignes 148-198) :**

```javascript
function updateMap(data) {
  const { pharmacies, userLocation } = data;

  // Supprimer les anciens markers
  pharmacyMarkers.forEach((marker) => map.removeLayer(marker));
  pharmacyMarkers.length = 0;
  if (userMarker) {
    map.removeLayer(userMarker);
  }

  // Ajouter marker utilisateur
  if (userLocation) {
    userMarker = L.marker([userLocation.latitude, userLocation.longitude], {
      icon: userIcon,
      title: "Votre position",
    }).addTo(map);

    // Centrer la carte sur l'utilisateur
    map.setView([userLocation.latitude, userLocation.longitude], 14);
  }

  // Ajouter markers pharmacies
  pharmacies.forEach((pharmacy) => {
    const color = getMarkerColor(pharmacy);
    const marker = L.marker([pharmacy.latitude, pharmacy.longitude], {
      icon: createIcon(color),
      title: pharmacy.name,
    });

    // Popup avec infos
    const popupContent = `
      <div class="pharmacy-popup">
        <h3>${pharmacy.name}</h3>
        <p>${pharmacy.address}</p>
        ${
          pharmacy.distanceKm
            ? `<p class="distance">${pharmacy.distanceKm.toFixed(1)} km</p>`
            : ""
        }
      </div>
    `;

    marker.bindPopup(popupContent);

    // Envoyer un message quand on clique sur le marker
    marker.on("click", () => {
      window.ReactNativeWebView.postMessage(
        JSON.stringify({
          type: "MARKER_CLICK",
          pharmacyId: pharmacy.id,
        })
      );
    });

    marker.addTo(map);
    pharmacyMarkers.push(marker);
  });
}

// Écouter les messages de React Native
document.addEventListener("message", (event) => {
  try {
    const data = JSON.parse(event.data);
    if (data.type === "UPDATE_DATA") {
      updateMap(data);
    }
  } catch (e) {
    console.error("Error parsing message:", e);
  }
});

// Pour Android
window.addEventListener("message", (event) => {
  try {
    const data = JSON.parse(event.data);
    if (data.type === "UPDATE_DATA") {
      updateMap(data);
    }
  } catch (e) {
    console.error("Error parsing message:", e);
  }
});
```

**⚠️ PROBLÈME POTENTIEL #2 :** La WebView pourrait ne pas recevoir les messages ou ne pas être complètement chargée avant l'envoi du premier message.

---

## 🔧 Solutions proposées

### Solution 1 : Ajouter un délai après le chargement de la WebView

**Modifier LeafletMapView.tsx :**

```typescript
const [isWebViewReady, setIsWebViewReady] = useState(false);

useEffect(() => {
  if (
    webViewRef.current &&
    userLocation &&
    isWebViewReady &&
    pharmacies.length > 0
  ) {
    const message = JSON.stringify({
      type: "UPDATE_DATA",
      pharmacies,
      userLocation,
    });
    webViewRef.current.postMessage(message);
    console.log("📤 Message envoyé à WebView:", message);
  }
}, [pharmacies, userLocation, isWebViewReady]);

return (
  <View style={[styles.container, style]}>
    <WebView
      ref={webViewRef}
      originWhitelist={["*"]}
      source={{ html: htmlContent }}
      style={styles.webview}
      javaScriptEnabled={true}
      domStorageEnabled={true}
      onMessage={handleMessage}
      onLoad={() => {
        console.log("✅ WebView chargée");
        setIsWebViewReady(true);
      }}
      scrollEnabled={false}
      showsHorizontalScrollIndicator={false}
      showsVerticalScrollIndicator={false}
    />
  </View>
);
```

### Solution 2 : Ajouter des logs de debug dans la WebView

**Ajouter dans le HTML de la WebView (juste avant `</script>`) :**

```javascript
console.log("🗺️ Leaflet initialisé");
console.log("📍 Position par défaut:", [11.8636, -15.5984]);

// Log quand on reçoit des données
document.addEventListener("message", (event) => {
  try {
    const data = JSON.parse(event.data);
    console.log("📥 Message reçu (document):", data);
    if (data.type === "UPDATE_DATA") {
      console.log("📍 Pharmacies reçues:", data.pharmacies.length);
      console.log("📍 User location:", data.userLocation);
      updateMap(data);
    }
  } catch (e) {
    console.error("❌ Error parsing message:", e);
  }
});

window.addEventListener("message", (event) => {
  try {
    const data = JSON.parse(event.data);
    console.log("📥 Message reçu (window):", data);
    if (data.type === "UPDATE_DATA") {
      console.log("📍 Pharmacies reçues:", data.pharmacies.length);
      console.log("📍 User location:", data.userLocation);
      updateMap(data);
    }
  } catch (e) {
    console.error("❌ Error parsing message:", e);
  }
});
```

### Solution 3 : Ajouter des logs dans PharmaciesScreen

**Ajouter dans initializeSearch :**

```typescript
const initializeSearch = async () => {
  console.log("🔍 Initialisation recherche pharmacies...");
  await getUserLocation();
  const results = await searchNearby(5);
  console.log("✅ Pharmacies trouvées:", results.length);
  console.log("📍 Données pharmacies:", results);
  console.log("📍 Position utilisateur:", userLocation);
};
```

### Solution 4 : Forcer un re-render après le chargement

**Ajouter dans LeafletMapView.tsx :**

```typescript
const webViewRef = useRef<WebView>(null);
const [webViewKey, setWebViewKey] = useState(0);

// Forcer le refresh de la WebView si nécessaire
useEffect(() => {
  if (pharmacies.length > 0 && userLocation) {
    setWebViewKey((prev) => prev + 1);
  }
}, [pharmacies.length]);

<WebView
  key={webViewKey}
  ref={webViewRef}
  // ... autres props
/>;
```

---

## 🧪 Tests de diagnostic

### Test 1 : Vérifier que le backend retourne des données

**Ouvrir Swagger UI :**

```bash
http://localhost:8080/swagger-ui.html
```

**Endpoint à tester :**

```
POST /api/v1/pharmacies/search
```

**Body :**

```json
{
  "latitude": 11.8636,
  "longitude": -15.5984,
  "radiusKm": 5,
  "limit": 10
}
```

**Résultat attendu :** 3 pharmacies dans le tableau de réponse.

### Test 2 : Vérifier les logs React Native

**Dans le terminal Expo :**

1. Chercher les logs `📤 Message envoyé à WebView:`
2. Chercher les logs `✅ Pharmacies trouvées:`
3. Vérifier que `pharmacies.length > 0`

### Test 3 : Vérifier les logs de la WebView

**Activer React DevTools :**

```bash
# Android
adb logcat *:S ReactNative:V ReactNativeJS:V
```

**Chercher :**

- `🗺️ Leaflet initialisé`
- `📥 Message reçu`
- `📍 Pharmacies reçues: X`

---

## 🎯 Checklist de vérification

- [ ] Backend démarre sans erreur
- [ ] Migration V10 appliquée (3 pharmacies en base)
- [ ] Position GPS simulée sur émulateur (Bissau: 11.8636, -15.5984)
- [ ] Permission géolocalisation accordée
- [ ] Onglet "Pharmacies" accessible
- [ ] Bouton "🗺️ Carte" cliquable
- [ ] Carte OpenStreetMap s'affiche
- [ ] Position utilisateur (cercle bleu) visible
- [ ] 3 markers pharmacies visibles
- [ ] Popups s'affichent au clic

---

## 🚀 Actions immédiates recommandées

### 1. Ajouter des logs partout

```typescript
// Dans PharmaciesScreen.tsx
useEffect(() => {
  console.log("🗺️ PharmaciesScreen mounted");
  initializeSearch();
}, []);

useEffect(() => {
  console.log("📊 Pharmacies updated:", pharmacies.length, pharmacies);
  console.log("📍 User location updated:", userLocation);
}, [pharmacies, userLocation]);

// Dans LeafletMapView.tsx
useEffect(() => {
  console.log("🗺️ LeafletMapView props:", {
    pharmaciesCount: pharmacies.length,
    userLocation,
  });
}, [pharmacies, userLocation]);
```

### 2. Vérifier la requête réseau

**Ouvrir React Native Debugger ou Flipper :**

- Network tab
- Vérifier la requête `POST /api/v1/pharmacies/search`
- Vérifier la réponse (devrait contenir 3 pharmacies)

### 3. Tester avec des données hardcodées

**Remplacer temporairement dans PharmaciesScreen :**

```typescript
const [testPharmacies] = useState<Pharmacy[]>([
  {
    id: "1",
    name: "Pharmacie Centrale TEST",
    address: "Avenue Amilcar Cabral",
    latitude: 11.8636,
    longitude: -15.5984,
    distanceKm: 0.1,
    isOpenNow: true,
    is24h: true,
    isOnDuty: false,
    isNightPharmacy: false,
  },
  {
    id: "2",
    name: "Pharmacie du Port TEST",
    address: "Rua do Porto",
    latitude: 11.865,
    longitude: -15.597,
    distanceKm: 0.2,
    isOpenNow: true,
    is24h: false,
    isOnDuty: true,
    isNightPharmacy: false,
  },
  {
    id: "3",
    name: "Pharmacie de Nuit TEST",
    address: "Avenue 14 de Novembro",
    latitude: 11.862,
    longitude: -15.5995,
    distanceKm: 0.3,
    isOpenNow: true,
    is24h: false,
    isOnDuty: false,
    isNightPharmacy: true,
  },
]);

// Passer testPharmacies à LeafletMapView
<LeafletMapView
  pharmacies={testPharmacies} // Au lieu de pharmacies
  userLocation={{ latitude: 11.8636, longitude: -15.5984 }} // Hardcodé
  onMarkerPress={(pharmacyId) => {
    console.log("Marker cliqué:", pharmacyId);
  }}
  style={styles.map}
/>;
```

Si ça fonctionne avec des données hardcodées, le problème vient du backend ou de l'API.

---

## 📝 Résumé du diagnostic

**Problèmes possibles identifiés :**

1. ❓ WebView n'est pas complètement chargée avant l'envoi des données
2. ❓ `useEffect` ne se déclenche pas car `userLocation` est `null` initialement
3. ❓ Communication `postMessage` entre React Native et WebView ne fonctionne pas
4. ❓ Backend ne retourne pas les pharmacies
5. ❓ Position GPS non simulée sur l'émulateur

**Probabilité :**

- **Haute (80%)** : WebView pas prête ou `userLocation` null
- **Moyenne (15%)** : Backend ne retourne pas les données
- **Faible (5%)** : Problème Leaflet ou OpenStreetMap

---

## 🎯 Plan d'action immédiat

1. ✅ Ajouter les logs de debug (Solution 1, 2, 3)
2. ✅ Tester avec données hardcodées (Solution 3)
3. ✅ Vérifier le backend via Swagger
4. ✅ Simuler position GPS sur émulateur
5. ✅ Relancer l'app avec `--clear`

---

**🔍 Une fois les logs ajoutés, partagez-moi les résultats et on trouvera le problème exact !**
