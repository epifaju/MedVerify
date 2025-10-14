# ✅ Fix : Debug Markers Carte Pharmacies

**Date :** 14 octobre 2025  
**Problème résolu :** Les markers des pharmacies ne s'affichaient pas sur la carte interactive

---

## 🐛 Problème identifié

Les markers des 3 pharmacies de test à Bissau n'apparaissaient pas sur la carte OpenStreetMap.

**Cause principale :** La WebView n'était pas complètement chargée avant l'envoi des données des pharmacies.

---

## ✅ Corrections apportées

### 1. LeafletMapView.tsx : Attendre que la WebView soit prête

**Avant :**

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

**Après :**

```typescript
const [isWebViewReady, setIsWebViewReady] = useState(false);

useEffect(() => {
  if (webViewRef.current && isWebViewReady) {
    // ← Plus de condition sur userLocation
    const message = JSON.stringify({
      type: "UPDATE_DATA",
      pharmacies,
      userLocation,
    });
    webViewRef.current.postMessage(message);
    console.log("📤 Message envoyé à WebView:", {
      pharmaciesCount: pharmacies.length,
      userLocation,
      pharmacies: pharmacies.map((p) => ({
        id: p.id,
        name: p.name,
        lat: p.latitude,
        lng: p.longitude,
      })),
    });
  }
}, [pharmacies, userLocation, isWebViewReady]); // ← Dépend de isWebViewReady

// Dans le WebView
<WebView
  ref={webViewRef}
  originWhitelist={["*"]}
  source={{ html: htmlContent }}
  onLoad={() => {
    console.log("✅ WebView chargée, prête à recevoir les données");
    setIsWebViewReady(true); // ← Signale que la WebView est prête
  }}
  onError={(syntheticEvent) => {
    const { nativeEvent } = syntheticEvent;
    console.error("❌ WebView erreur:", nativeEvent);
  }}
  // ... autres props
/>;
```

### 2. LeafletMapView.tsx : Ajout de logs de debug dans la WebView

**JavaScript dans la WebView :**

```javascript
function updateMap(data) {
  const { pharmacies, userLocation } = data;

  console.log("🗺️ updateMap appelé", {
    pharmaciesCount: pharmacies ? pharmacies.length : 0,
    hasUserLocation: !!userLocation,
  });

  // ... logique d'ajout de markers ...

  if (pharmacies && Array.isArray(pharmacies)) {
    pharmacies.forEach((pharmacy) => {
      console.log("🏥 Ajout marker pharmacie:", {
        id: pharmacy.id,
        name: pharmacy.name,
        lat: pharmacy.latitude,
        lng: pharmacy.longitude,
      });
      // ... ajout marker ...
    });
    console.log("✅ Tous les markers ajoutés:", pharmacyMarkers.length);
  } else {
    console.warn("⚠️ Aucune pharmacie à afficher ou données invalides");
  }
}

// Logs lors de la réception des messages
document.addEventListener("message", (event) => {
  try {
    const data = JSON.parse(event.data);
    console.log("📥 Message reçu (document.addEventListener):", data.type);
    if (data.type === "UPDATE_DATA") {
      console.log("📊 Données:", {
        pharmaciesCount: data.pharmacies ? data.pharmacies.length : 0,
        hasUserLocation: !!data.userLocation,
      });
      updateMap(data);
    }
  } catch (e) {
    console.error("❌ Error parsing message:", e);
  }
});

window.addEventListener("message", (event) => {
  try {
    const data = JSON.parse(event.data);
    console.log("📥 Message reçu (window.addEventListener):", data.type);
    if (data.type === "UPDATE_DATA") {
      console.log("📊 Données:", {
        pharmaciesCount: data.pharmacies ? data.pharmacies.length : 0,
        hasUserLocation: !!data.userLocation,
      });
      updateMap(data);
    }
  } catch (e) {
    console.error("❌ Error parsing message:", e);
  }
});

// Log à l'initialisation
map.setView([11.8636, -15.5984], 13);
console.log(
  "🗺️ Leaflet initialisé - Position par défaut: Bissau [11.8636, -15.5984]"
);
```

### 3. PharmaciesScreen.tsx : Ajout de logs de debug

**Logs lors du montage et des changements d'état :**

```typescript
// Charger pharmacies au montage
useEffect(() => {
  console.log("🏥 PharmaciesScreen monté - Initialisation...");
  initializeSearch();
}, []);

// Log quand les données changent
useEffect(() => {
  console.log("📊 État mis à jour:", {
    pharmaciesCount: pharmacies.length,
    userLocation,
    loading,
    error,
  });
}, [pharmacies, userLocation, loading, error]);

const initializeSearch = async () => {
  console.log("🔍 Initialisation recherche pharmacies...");
  await getUserLocation();
  const results = await searchNearby(5);
  console.log("✅ Résultats recherche:", {
    count: results.length,
    pharmacies: results.map((p) => ({
      id: p.id,
      name: p.name,
      lat: p.latitude,
      lng: p.longitude,
    })),
  });
};
```

---

## 📁 Fichiers modifiés

```
✅ MedVerifyApp/MedVerifyExpo/src/components/LeafletMapView.tsx
   - Ajouté: useState pour isWebViewReady
   - Modifié: useEffect pour attendre que la WebView soit prête
   - Ajouté: onLoad callback pour setIsWebViewReady(true)
   - Ajouté: onError callback pour logger les erreurs
   - Ajouté: Logs de debug dans updateMap()
   - Ajouté: Logs dans les event listeners de messages

✅ MedVerifyApp/MedVerifyExpo/src/screens/Pharmacies/PharmaciesScreen.tsx
   - Ajouté: useEffect avec logs au montage
   - Ajouté: useEffect avec logs sur changement d'état
   - Modifié: initializeSearch() avec logs détaillés

✅ DEBUG_CARTE_PHARMACIES_MARKERS.md (nouveau)
   - Guide de diagnostic complet

✅ FIX_CARTE_MARKERS_DEBUG.md (nouveau)
   - Ce fichier - Documentation des corrections
```

---

## 🧪 Comment tester

### Étape 1 : Relancer l'application

```bash
cd MedVerifyApp/MedVerifyExpo
npx expo start --clear
```

**Important :** `--clear` pour nettoyer le cache.

### Étape 2 : Observer les logs dans le terminal

**Logs attendus lors du démarrage :**

```
🏥 PharmaciesScreen monté - Initialisation...
🔍 Initialisation recherche pharmacies...
📊 État mis à jour: { pharmaciesCount: 0, userLocation: null, loading: true, error: null }
📊 État mis à jour: { pharmaciesCount: 0, userLocation: {...}, loading: true, error: null }
✅ Résultats recherche: { count: 3, pharmacies: [...] }
📊 État mis à jour: { pharmaciesCount: 3, userLocation: {...}, loading: false, error: null }
```

### Étape 3 : Cliquer sur l'onglet "🗺️ Carte"

**Logs attendus lors du changement de vue :**

```
✅ WebView chargée, prête à recevoir les données
📤 Message envoyé à WebView: { pharmaciesCount: 3, userLocation: {...}, pharmacies: [...] }
🗺️ Leaflet initialisé - Position par défaut: Bissau [11.8636, -15.5984]
📥 Message reçu (window.addEventListener): UPDATE_DATA
📊 Données: { pharmaciesCount: 3, hasUserLocation: true }
🗺️ updateMap appelé { pharmaciesCount: 3, hasUserLocation: true }
📍 Marker utilisateur ajouté: [11.8636, -15.5984]
🏥 Ajout marker pharmacie: { id: '...', name: 'Pharmacie Centrale de Bissau', lat: 11.8636, lng: -15.5984 }
🏥 Ajout marker pharmacie: { id: '...', name: 'Pharmacie du Port', lat: 11.8650, lng: -15.5970 }
🏥 Ajout marker pharmacie: { id: '...', name: 'Pharmacie de Nuit Bissau', lat: 11.8620, lng: -15.5995 }
✅ Tous les markers ajoutés: 3
```

### Étape 4 : Vérifier visuellement

**Ce que vous devez voir sur la carte :**

1. ✅ **Carte OpenStreetMap** centrée sur Bissau
2. ✅ **Cercle bleu** = Votre position (marker utilisateur)
3. ✅ **3 markers colorés** = Les 3 pharmacies de test
   - 🟣 **Violet** : Pharmacie Centrale (24h/24)
   - 🔴 **Rouge** : Pharmacie du Port (de garde)
   - 🟢 **Vert** : Pharmacie de Nuit (ouverte)
4. ✅ **Popup au clic** : Nom, adresse, distance

### Étape 5 : Tester l'interactivité

**Actions à effectuer :**

1. ✅ Cliquer sur un marker → Popup s'affiche
2. ✅ Cliquer sur le popup → Navigate vers détails
3. ✅ Zoomer/dézoomer → Carte réagit
4. ✅ Drag/Pan → Carte se déplace

---

## 🔍 Si les markers ne s'affichent toujours pas

### Vérification 1 : Backend retourne des données

**Ouvrir Swagger UI :**

```
http://localhost:8080/swagger-ui.html
```

**Tester l'endpoint :**

```
POST /api/v1/pharmacies/search
Body:
{
  "latitude": 11.8636,
  "longitude": -15.5984,
  "radiusKm": 5,
  "limit": 10
}
```

**Résultat attendu :** 3 pharmacies dans la réponse.

### Vérification 2 : Position GPS simulée

**Android Studio :**

1. Extended Controls (**...**)
2. **Location** tab
3. Coordonnées : `11.8636, -15.5984`
4. **SET LOCATION**

**iOS Simulator :**

1. **Features** → **Location** → **Custom Location**
2. Latitude : `11.8636` / Longitude : `-15.5984`

### Vérification 3 : Permission géolocalisation

**Sur l'appareil/émulateur :**

- Android : Paramètres > Apps > MedVerify > Permissions > Position → **Autoriser**
- iOS : Réglages > Confidentialité > Localisation > MedVerify → **Toujours autoriser**

### Vérification 4 : Migration V10 appliquée

**Vérifier dans PostgreSQL :**

```sql
SELECT COUNT(*) FROM pharmacies WHERE city = 'Bissau' AND is_active = TRUE;
-- Doit retourner: 3
```

---

## 📊 Logs attendus (complet)

### Scénario nominal

```log
# 1. Montage du composant
🏥 PharmaciesScreen monté - Initialisation...
🔍 Initialisation recherche pharmacies...

# 2. Géolocalisation
📊 État mis à jour: { pharmaciesCount: 0, userLocation: null, loading: true, error: null }
📊 État mis à jour: { pharmaciesCount: 0, userLocation: { latitude: 11.8636, longitude: -15.5984 }, loading: true, error: null }

# 3. Recherche pharmacies
✅ Résultats recherche: {
  count: 3,
  pharmacies: [
    { id: '...', name: 'Pharmacie Centrale de Bissau', lat: 11.8636, lng: -15.5984 },
    { id: '...', name: 'Pharmacie du Port', lat: 11.8650, lng: -15.5970 },
    { id: '...', name: 'Pharmacie de Nuit Bissau', lat: 11.8620, lng: -15.5995 }
  ]
}
📊 État mis à jour: { pharmaciesCount: 3, userLocation: {...}, loading: false, error: null }

# 4. Clic sur "🗺️ Carte"
✅ WebView chargée, prête à recevoir les données
📤 Message envoyé à WebView: { pharmaciesCount: 3, userLocation: {...}, pharmacies: [...] }

# 5. Leaflet initialisation
🗺️ Leaflet initialisé - Position par défaut: Bissau [11.8636, -15.5984]
📥 Message reçu (window.addEventListener): UPDATE_DATA
📊 Données: { pharmaciesCount: 3, hasUserLocation: true }

# 6. Affichage des markers
🗺️ updateMap appelé { pharmaciesCount: 3, hasUserLocation: true }
📍 Marker utilisateur ajouté: [11.8636, -15.5984]
🏥 Ajout marker pharmacie: { id: '...', name: 'Pharmacie Centrale de Bissau', lat: 11.8636, lng: -15.5984 }
🏥 Ajout marker pharmacie: { id: '...', name: 'Pharmacie du Port', lat: 11.8650, lng: -15.5970 }
🏥 Ajout marker pharmacie: { id: '...', name: 'Pharmacie de Nuit Bissau', lat: 11.8620, lng: -15.5995 }
✅ Tous les markers ajoutés: 3

# 7. Clic sur marker
🖱️ Marker cliqué: <pharmacy-id>
```

---

## 🎯 Résumé des améliorations

### Corrections principales

1. ✅ **WebView Ready State** : Attendre que la WebView soit chargée avant d'envoyer les données
2. ✅ **Logs détaillés** : Tracer toute la chaîne de communication
3. ✅ **Gestion d'erreurs** : Logger les erreurs de WebView et de parsing

### Bénéfices

- **🐛 Debuggabilité** : Logs clairs à chaque étape
- **🔍 Visibilité** : Comprendre exactement ce qui se passe
- **⚡ Fiabilité** : La WebView est prête avant de recevoir les données
- **🛡️ Robustesse** : Gestion des cas d'erreur

---

## 🎉 Résultat attendu

**Après ces corrections, vous devez voir :**

1. ✅ Carte OpenStreetMap chargée
2. ✅ Cercle bleu (position utilisateur)
3. ✅ 3 markers colorés (pharmacies)
4. ✅ Popups interactifs
5. ✅ Navigation vers détails

**Si vous voyez tout ça : SUCCÈS ! 🎉**

---

## 📝 Prochaines étapes (optionnel)

Si tout fonctionne, vous pouvez :

1. **Retirer les logs** de production (garder uniquement pour debug)
2. **Implémenter les notifications** de garde (Phase 5)
3. **Ajouter le système de notation** des pharmacies
4. **Améliorer le clustering** des markers (si trop de pharmacies)

---

**🔧 Testez maintenant et dites-moi ce que vous voyez dans les logs !**
