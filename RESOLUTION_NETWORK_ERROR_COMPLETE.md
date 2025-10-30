# ✅ Résolution Complète : Erreur Network Error

## 🎯 Problème Résolu

L'erreur `Network Error` était causée par le fait que votre téléphone Android n'était pas sur le même réseau WiFi que votre PC. Le téléphone était connecté via USB mais ne pouvait pas atteindre l'IP `192.168.1.16` du PC.

## ✅ Solution Appliquée

### 1. Configuration du tunnel USB avec `adb reverse`

Le téléphone est maintenant connecté via USB avec un tunnel TCP configuré :

```powershell
adb reverse tcp:8080 tcp:8080
```

**Vérification** :

```powershell
adb reverse --list
# Résultat : UsbFfs tcp:8080 tcp:8080 ✓
```

### 2. Modification de l'URL de l'API

**Fichier modifié** : `MedVerifyApp/src/config/constants.ts`

**Avant** :

```typescript
export const API_BASE_URL = "http://192.168.1.16:8080/api/v1";
```

**Après** :

```typescript
export const API_BASE_URL = "http://localhost:8080/api/v1";
```

**Explication** : Avec `adb reverse`, le téléphone peut maintenant accéder au backend du PC via `localhost:8080` directement.

---

## 📋 Configuration Actuelle

### ✅ Backend

- Status : ✅ En cours d'exécution
- Port : 8080
- Écoute sur : `0.0.0.0:8080` (toutes interfaces)
- Accessible depuis PC : `http://localhost:8080` ✓

### ✅ Téléphone Android

- Connection : USB (via ADB)
- Tunnel : `adb reverse tcp:8080 tcp:8080` ✓
- URL API : `http://localhost:8080/api/v1` ✓

---

## 🚀 Prochaines Étapes

### 1. Redémarrer l'application mobile

Après ces modifications, vous devez **recompiler et relancer l'application** :

```powershell
cd MedVerifyApp
npm run android
```

### 2. Tester la connexion

Essayez de vous connecter avec :

- **Email** : `admin@medverify.gw`
- **Mot de passe** : `Admin@123456`

---

## 🔄 Important : Réexécuter `adb reverse` si nécessaire

**⚠️ Note** : Le tunnel `adb reverse` doit être réexécuté à chaque fois que :

- Vous redéconnectez le téléphone
- Vous redémarrez ADB
- Vous redémarrez le téléphone

**Solution rapide** : Créez un fichier `setup-usb-connection.ps1` :

```powershell
# setup-usb-connection.ps1
Write-Host "Configuration de la connexion USB..." -ForegroundColor Cyan

# Vérifier que le téléphone est connecté
$devices = adb devices
if ($devices -notmatch "device$") {
    Write-Host "❌ Aucun appareil connecté via USB" -ForegroundColor Red
    exit 1
}

# Configurer le tunnel
adb reverse tcp:8080 tcp:8080
Write-Host "✅ Tunnel USB configuré : localhost:8080 → PC:8080" -ForegroundColor Green

# Vérifier
Write-Host "`nTunnels actifs :" -ForegroundColor Yellow
adb reverse --list
```

---

## 🌐 Alternative : Utiliser le WiFi (si disponible)

Si vous préférez utiliser le WiFi au lieu de USB :

### Étapes :

1. **Connecter le téléphone au même WiFi que le PC**
2. **Vérifier l'IP du PC** :
   ```powershell
   ipconfig | findstr IPv4
   ```
3. **Mettre à jour `constants.ts`** :
   ```typescript
   export const API_BASE_URL = "http://192.168.1.16:8080/api/v1";
   ```
4. **Configurer le firewall Windows** (si nécessaire) :
   ```powershell
   netsh advfirewall firewall add rule name="MedVerify Backend Port 8080" dir=in action=allow protocol=TCP localport=8080
   ```

---

## ✅ Checklist de Vérification

Avant de tester la connexion, vérifiez :

- [x] Backend démarré sur `localhost:8080`
- [x] Téléphone connecté via USB
- [x] `adb reverse tcp:8080 tcp:8080` exécuté
- [x] URL dans `constants.ts` = `http://localhost:8080/api/v1`
- [ ] Application mobile recompilée et relancée
- [ ] Test de connexion effectué

---

## 🐛 Troubleshooting

### Si l'erreur Network Error persiste :

1. **Vérifier que le tunnel est actif** :

   ```powershell
   adb reverse --list
   ```

   Doit afficher : `UsbFfs tcp:8080 tcp:8080`

2. **Réexécuter le tunnel** :

   ```powershell
   adb reverse --remove-all
   adb reverse tcp:8080 tcp:8080
   ```

3. **Vérifier que le backend tourne** :

   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8080/actuator/health"
   ```

4. **Vérifier la connexion ADB** :
   ```powershell
   adb devices
   ```
   Doit afficher votre appareil avec `device` (pas `unauthorized`)

---

## 📝 Notes

- **USB (actuel)** : Nécessite `adb reverse` à chaque reconnexion
- **WiFi** : Plus pratique pour utilisation longue durée, mais nécessite même réseau
- **Émulateur** : Utiliser `10.0.2.2` au lieu de `localhost` ou IP réelle

---

## 🎉 Résultat Attendu

Après ces corrections, la connexion devrait fonctionner et vous ne devriez plus voir l'erreur `Network Error`. Le message d'erreur amélioré que nous avons ajouté vous aidera également à diagnostiquer tout problème futur.


