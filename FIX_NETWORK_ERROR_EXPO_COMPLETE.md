# ✅ Fix Complet : Erreur Network Error dans MedVerifyExpo

## 🐛 Problème Identifié

L'application **MedVerifyExpo** affichait toujours l'erreur :

```
❌ Erreur de connexion: [AxiosError: Network Error]
```

## 🔍 Causes Trouvées

1. **URL incorrecte** : L'application utilisait `http://192.168.1.16:8080/api/v1` au lieu de `http://localhost:8080/api/v1`
2. **Tunnel USB non configuré** : Le tunnel `adb reverse` n'était pas actif
3. **Configuration dans le mauvais projet** : Les corrections initiales étaient dans `MedVerifyApp` mais l'app utilisait `MedVerifyExpo`

---

## ✅ Corrections Appliquées

### 1. Correction de l'URL API dans MedVerifyExpo

**Fichier modifié** : `MedVerifyApp/MedVerifyExpo/src/config/constants.ts`

**Avant** :

```typescript
export const API_CONFIG = {
  BASE_URL: "http://192.168.1.16:8080/api/v1", // Votre IP locale
  TIMEOUT: 30000,
};
```

**Après** :

```typescript
export const API_CONFIG = {
  // Pour appareil physique connecté via USB : utiliser localhost (avec adb reverse)
  // Pour appareil physique sur WiFi : utiliser l'IP du PC (192.168.1.16)
  // Pour émulateur Android : utiliser 10.0.2.2
  BASE_URL: process.env.API_BASE_URL || "http://localhost:8080/api/v1",
  TIMEOUT: 30000,
};
```

### 2. Configuration du tunnel USB

**Commande exécutée** :

```powershell
adb reverse tcp:8080 tcp:8080
```

**Vérification** :

```powershell
adb reverse --list
# Résultat attendu : UsbFfs tcp:8080 tcp:8080 ✓
```

### 3. Correction de DebugScreen.tsx

**Fichier modifié** : `MedVerifyApp/MedVerifyExpo/src/screens/DebugScreen.tsx`

- Remplacement des URLs hardcodées par `API_CONFIG.BASE_URL`
- Correction de l'import pour utiliser `API_CONFIG` au lieu de `API_BASE_URL`
- Correction du mot de passe de test (Admin@123456)

---

## 📋 Configuration Actuelle

### ✅ Backend

- Status : ✅ En cours d'exécution
- Port : 8080
- Accessible depuis PC : `http://localhost:8080` ✓

### ✅ Téléphone Android

- Connection : USB (via ADB) ✓
- Tunnel : `adb reverse tcp:8080 tcp:8080` ✓
- URL API : `http://localhost:8080/api/v1` ✓

### ✅ Application

- Projet utilisé : **MedVerifyExpo**
- URL configurée : `http://localhost:8080/api/v1` ✓

---

## 🚀 Prochaines Étapes

### 1. Redémarrer l'application Expo

```powershell
cd MedVerifyApp/MedVerifyExpo
npx expo start --clear
```

### 2. Tester la connexion

Essayez de vous connecter avec :

- **Email** : `admin@medverify.gw`
- **Mot de passe** : `Admin@123456`

---

## 🔄 Important : Réexécuter `adb reverse` si nécessaire

Le tunnel `adb reverse` doit être réexécuté à chaque fois que :

- Vous redéconnectez le téléphone
- Vous redémarrez ADB
- Vous redémarrez le téléphone

**Commande rapide** :

```powershell
adb reverse tcp:8080 tcp:8080
```

Ou utiliser le script (après correction) :

```powershell
.\setup-usb-connection.ps1
```

---

## ✅ Checklist de Vérification

Avant de tester, vérifiez :

- [x] Backend démarré sur `localhost:8080`
- [x] Téléphone connecté via USB
- [x] `adb reverse tcp:8080 tcp:8080` exécuté
- [x] URL dans `MedVerifyExpo/src/config/constants.ts` = `http://localhost:8080/api/v1`
- [ ] Application Expo relancée avec `--clear`
- [ ] Test de connexion effectué

---

## 🐛 Troubleshooting

### Si l'erreur Network Error persiste :

1. **Vérifier que le tunnel est actif** :

   ```powershell
   adb reverse --list
   ```

   Doit afficher : `UsbFfs tcp:8080 tcp:8080`

2. **Vérifier que le backend tourne** :

   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8080/actuator/health"
   ```

3. **Vérifier la connexion ADB** :

   ```powershell
   adb devices
   ```

   Doit afficher votre appareil avec `device`

4. **Redémarrer Expo avec cache clear** :

   ```powershell
   npx expo start --clear
   ```

5. **Vérifier l'URL dans l'app** :
   - Ouvrir `MedVerifyApp/MedVerifyExpo/src/config/constants.ts`
   - Confirmer que `BASE_URL` est `http://localhost:8080/api/v1`

---

## 📝 Notes

- **Projet utilisé** : `MedVerifyExpo` (pas `MedVerifyApp`)
- **Tunnel USB** : Nécessaire pour connecter le téléphone au backend du PC
- **Alternative WiFi** : Si vous préférez le WiFi, changez l'URL vers `http://192.168.1.16:8080/api/v1` et connectez-vous au même réseau

---

## ✅ Résultat Attendu

Après ces corrections :

- ✅ L'erreur `Network Error` ne devrait plus apparaître
- ✅ La connexion devrait fonctionner avec les identifiants admin
- ✅ L'application peut communiquer avec le backend

---

## 📊 Fichiers Modifiés

1. `MedVerifyApp/MedVerifyExpo/src/config/constants.ts` - URL corrigée
2. `MedVerifyApp/MedVerifyExpo/src/screens/DebugScreen.tsx` - URLs hardcodées remplacées
3. Tunnel USB configuré via `adb reverse`

---

## 🔗 Référence

Voir aussi : `RESOLUTION_NETWORK_ERROR_COMPLETE.md` pour les détails sur la configuration USB.


