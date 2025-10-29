# ✅ Correction : Erreur Network Error lors de la connexion

## 🐛 Problème Identifié

L'application affiche l'erreur :

```
❌ Erreur de connexion: [AxiosError: Network Error]
```

Cette erreur indique que l'application ne peut pas se connecter au backend.

## ✅ Corrections Appliquées

### 1. Amélioration de la gestion des erreurs réseau

**Fichier modifié** : `MedVerifyExpo/src/store/slices/authSlice.ts`

- ✅ Détection spécifique des erreurs réseau (ERR_NETWORK)
- ✅ Messages d'erreur clairs avec instructions de résolution
- ✅ Instructions différentes selon le type de connexion (USB ou WiFi)

**Messages d'erreur améliorés** :

```
Erreur de connexion réseau. Vérifiez que:
• Le backend est démarré (port 8080)
• Pour USB: exécutez "adb reverse tcp:8080 tcp:8080"
• Pour WiFi: assurez-vous que le téléphone est sur le même réseau
```

## 🔧 Solutions selon votre configuration

### Solution 1 : Connexion USB (Recommandé pour développement)

1. **Connecter votre téléphone via USB** et activer le débogage USB

2. **Configurer le tunnel ADB** :

   ```powershell
   adb reverse tcp:8080 tcp:8080
   ```

3. **Vérifier que le tunnel est actif** :

   ```powershell
   adb reverse --list
   ```

   Doit afficher : `UsbFfs tcp:8080 tcp:8080`

4. **Modifier l'URL dans `MedVerifyExpo/src/config/constants.ts`** :
   ```typescript
   export const API_BASE_URL =
     process.env.API_BASE_URL || "http://localhost:8080/api/v1";
   ```

### Solution 2 : Connexion WiFi

1. **Vérifier que le téléphone et le PC sont sur le même réseau WiFi**

2. **Trouver l'IP de votre PC** :

   - Windows : Ouvrir PowerShell et exécuter `ipconfig`
   - Rechercher l'adresse IPv4 (ex: `192.168.1.16`)

3. **Vérifier que le backend est accessible depuis le PC** :

   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8080/actuator/health"
   ```

4. **L'URL dans `MedVerifyExpo/src/config/constants.ts` doit être** :

   ```typescript
   export const API_BASE_URL =
     process.env.API_BASE_URL || "http://192.168.1.16:8080/api/v1";
   ```

   _(Remplacez `192.168.1.16` par l'IP de votre PC)_

5. **Désactiver le pare-feu Windows** (temporairement) ou ouvrir le port 8080

## 📋 Checklist de Vérification

Avant de tester la connexion :

- [ ] Backend démarré sur `localhost:8080`
- [ ] **Pour USB** :
  - [ ] Téléphone connecté via USB
  - [ ] Débogage USB activé
  - [ ] `adb reverse tcp:8080 tcp:8080` exécuté
  - [ ] URL configurée = `http://localhost:8080/api/v1`
- [ ] **Pour WiFi** :
  - [ ] Téléphone et PC sur le même réseau WiFi
  - [ ] IP du PC identifiée
  - [ ] URL configurée = `http://[IP_PC]:8080/api/v1`
  - [ ] Pare-feu configuré pour permettre les connexions
- [ ] Application Expo relancée avec `--clear` :
  ```powershell
  cd MedVerifyExpo
  npx expo start --clear
  ```

## 🧪 Test de Connexion

1. **Démarrer le backend** :

   ```powershell
   cd medverify-backend
   mvn spring-boot:run
   ```

2. **Démarrer l'application Expo** :

   ```powershell
   cd MedVerifyExpo
   npx expo start --clear
   ```

3. **Tester la connexion** avec :
   - Email : `admin@medverify.gw` ou votre email
   - Mot de passe : Votre mot de passe

## 🔍 Debugging

Si l'erreur persiste, vérifiez dans l'ordre :

1. **Vérifier le tunnel USB** :

   ```powershell
   adb reverse --list
   ```

2. **Vérifier la connexion ADB** :

   ```powershell
   adb devices
   ```

   Doit afficher votre appareil avec `device`

3. **Tester le backend depuis le PC** :

   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8080/actuator/health"
   ```

4. **Vérifier l'URL dans l'application** :

   - Ouvrir `MedVerifyExpo/src/config/constants.ts`
   - Vérifier que `API_BASE_URL` correspond à votre configuration

5. **Vérifier les logs du backend** :
   - Les logs doivent montrer les tentatives de connexion
   - Si aucune requête n'apparaît, le problème est réseau

## 📝 Notes Importantes

- **Le tunnel `adb reverse` doit être réexécuté** à chaque fois que :

  - Vous redéconnectez le téléphone
  - Vous redémarrez ADB
  - Vous redémarrez le téléphone

- **Pour WiFi** : L'IP de votre PC peut changer si vous changez de réseau. Vérifiez régulièrement.

- **Alternative** : Vous pouvez utiliser un script PowerShell automatique :
  ```powershell
  .\setup-usb-connection.ps1
  ```

## ✅ Résultat Attendu

Après ces corrections :

- ✅ Les messages d'erreur sont plus clairs et indiquent comment résoudre le problème
- ✅ L'application peut se connecter au backend avec la bonne configuration
- ✅ Les instructions sont affichées directement dans l'application en cas d'erreur

---

**Dernière mise à jour** : Résolution de l'erreur Network Error avec messages d'aide améliorés
