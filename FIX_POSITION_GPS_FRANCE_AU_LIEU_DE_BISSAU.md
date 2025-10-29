# ✅ PROBLÈME TROUVÉ : Position GPS en France au lieu de Bissau !

**Date :** 14 octobre 2025  
**Problème résolu :** Les pharmacies ne s'affichent pas car l'utilisateur est localisé en **France** au lieu de **Bissau**

---

## 🎯 Diagnostic final

### Ce qui s'est passé

**Logs reçus :**

```
LOG  📊 État mis à jour: {"userLocation": {"latitude": 48.9819387, "longitude": 1.6743258}}
LOG  ✅ Résultats recherche: {"count": 0, "pharmacies": []}
```

**Analyse :**

- ✅ Position utilisateur détectée : **48.9819387, 1.6743258**
- ❌ **C'est en France** (près de Paris) !
- ❌ Les pharmacies de test sont à **Bissau, Guinée-Bissau** (lat: 11.86, lon: -15.59)
- ❌ Distance : **~4500 km** → Aucune pharmacie dans un rayon de 5 km

### Pourquoi ça arrive ?

**Votre appareil/émulateur utilise la position réelle ou par défaut :**

- Appareil physique → Position GPS réelle (France)
- Émulateur sans config → Position par défaut (souvent San Francisco ou Londres)

---

## 🔧 Solutions

### ✅ Solution 1 : Test immédiat (position hardcodée) - **DÉJÀ IMPLÉMENTÉE**

J'ai modifié temporairement `PharmaciesScreen.tsx` pour **forcer la position à Bissau** :

```typescript
const initializeSearch = async () => {
  console.log("🔍 Initialisation recherche pharmacies...");

  // 🧪 TEST : Forcer position Bissau (temporaire)
  console.log("🧪 TEST: Utilisation position Bissau (hardcodée)");
  const results = await searchPharmacies({
    latitude: 11.8636,
    longitude: -15.5984,
    radiusKm: 5,
    limit: 10,
  });

  console.log("✅ Résultats recherche:", {
    count: results.length,
    pharmacies: results.map((p) => ({
      id: p.id,
      name: p.name,
      lat: p.latitude,
      lng: p.longitude,
    })),
  });

  // Mettre à jour userLocation pour la carte
  setTestUserLocation({ latitude: 11.8636, longitude: -15.5984 });
};
```

**🚀 Relancez l'app maintenant :**

```bash
# L'app devrait recharger automatiquement
# Sinon, appuyez sur 'r' dans le terminal Expo
```

**📊 Logs attendus :**

```
🧪 TEST: Utilisation position Bissau (hardcodée)
✅ Résultats recherche: { count: 3, pharmacies: [...] }
📤 Message envoyé à WebView: { pharmaciesCount: 3, ... }
🏥 Ajout marker pharmacie: { name: 'Pharmacie Centrale de Bissau', ... }
🏥 Ajout marker pharmacie: { name: 'Pharmacie du Port', ... }
🏥 Ajout marker pharmacie: { name: 'Pharmacie de Nuit Bissau', ... }
✅ Tous les markers ajoutés: 3
```

**🎉 Résultat attendu :**

- Carte centrée sur Bissau
- Cercle bleu (position utilisateur) à Bissau
- **3 markers pharmacies visibles** autour du cercle bleu
- Popups qui s'affichent au clic

---

### Solution 2 : Configurer position GPS sur émulateur (permanent)

#### **Android Studio Emulator**

1. Cliquer sur **...** (Extended Controls)
2. Onglet **Location**
3. Entrer :
   - **Latitude :** `11.8636`
   - **Longitude :** `-15.5984`
4. Cliquer **SET LOCATION**

#### **iOS Simulator**

1. Menu **Features** → **Location** → **Custom Location**
2. Entrer :
   - **Latitude :** `11.8636`
   - **Longitude :** `-15.5984`

#### **Appareil physique Android**

1. **Activer options développeur :**

   - Paramètres → À propos du téléphone
   - Appuyer 7 fois sur "Numéro de build"

2. **Activer position fictive :**

   - Paramètres → Système → Options pour les développeurs
   - "Sélectionner application de position fictive" → **Expo Go**

3. **Installer app de position fictive :**
   - Play Store → "Fake GPS Location" ou "GPS Joystick"
   - Chercher "Bissau, Guinée-Bissau"
   - Ou entrer manuellement : `11.8636, -15.5984`
   - Activer

---

## 📝 Vérification du backend (optionnel)

Si vous voulez confirmer que le backend fonctionne correctement :

### Via Swagger UI

```
URL: http://localhost:8080/swagger-ui.html
Endpoint: POST /api/v1/pharmacies/search
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

**Résultat attendu :** 3 pharmacies

### Logs backend attendus

```
🔍 Recherche pharmacies - User position: lat=11.8636, lng=-15.5984, radius=5.0km
✅ Pharmacies trouvées: 3
   🏥 Pharmacie Centrale de Bissau - lat=11.8636, lng=-15.5984, distance=0.00km
   🏥 Pharmacie du Port - lat=11.8650, lng=-15.5970, distance=0.16km
   🏥 Pharmacie de Nuit Bissau - lat=11.8620, lng=-15.5995, distance=0.23km
```

---

## 🎯 Pour retirer le test et utiliser la vraie géolocalisation

**Une fois la position GPS configurée sur l'émulateur, revenez au code original :**

```typescript
// Dans PharmaciesScreen.tsx
const initializeSearch = async () => {
  console.log("🔍 Initialisation recherche pharmacies...");
  await getUserLocation(); // ← Utiliser la vraie géolocalisation
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

Et supprimer :

```typescript
const [testUserLocation, setTestUserLocation] = useState<...>(null);  // À supprimer
```

---

## 📊 Résumé

### Avant (❌ Position France)

```
Position: 48.98°N, 1.67°E (France)
Pharmacies trouvées: 0
Carte: Vide (sauf cercle bleu)
```

### Après (✅ Position Bissau hardcodée)

```
Position: 11.86°N, -15.59°W (Bissau)
Pharmacies trouvées: 3
Carte: 3 markers + cercle bleu
```

### Plus tard (✅ Position Bissau émulateur)

```
Position: 11.86°N, -15.59°W (Bissau simulé)
Pharmacies trouvées: 3
Carte: 3 markers + cercle bleu
```

---

## 🚀 Action immédiate

**L'app devrait se recharger automatiquement avec le code modifié.**

**Si pas de rechargement automatique :**

```bash
# Dans le terminal Expo, appuyez sur 'r'
```

**Puis :**

1. Ouvrir l'app
2. Aller sur l'onglet "Pharmacies"
3. Cliquer sur "🗺️ Carte"

**✅ Vous devriez maintenant voir les 3 markers des pharmacies ! 🎉**

---

**📸 Partagez-moi une capture d'écran une fois que ça marche !**

