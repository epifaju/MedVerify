# 📱 Guide de Lancement Application Mobile MedVerify

## ✅ Prérequis

Avant de lancer l'application, assurez-vous que :

- ✅ Le backend Spring Boot est démarré sur `http://localhost:8080`
- ✅ Les dépendances npm sont installées (`npm install` terminé)
- ✅ Android Studio est installé (pour émulateur Android)
- ⬜ Un émulateur Android est démarré OU un appareil Android physique est connecté en mode Debug USB

## 🚀 Lancement sur Android

### Option 1 : Émulateur Android

1. **Démarrer un émulateur Android via Android Studio** :
   - Ouvrez Android Studio
   - Cliquez sur "Device Manager" (icône téléphone dans la barre latérale)
   - Sélectionnez un émulateur (ex: Pixel 5 API 33)
   - Cliquez sur le bouton Play ▶️

2. **Vérifier que l'émulateur est détecté** :

   ```powershell
   adb devices
   ```

   Vous devriez voir :

   ```
   List of devices attached
   emulator-5554   device
   ```

3. **Lancer l'application** :
   ```powershell
   cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp
   npm run android
   ```

### Option 2 : Appareil Physique Android

1. **Activer le mode développeur** sur votre téléphone :
   - Allez dans Paramètres → À propos du téléphone
   - Tapez 7 fois sur "Numéro de build"
   - Retournez dans Paramètres → Options développeur
   - Activez "Débogage USB"

2. **Connecter le téléphone** à votre PC via USB

3. **Autoriser le débogage** sur le téléphone (popup)

4. **Vérifier la connexion** :

   ```powershell
   adb devices
   ```

5. **Trouver l'IP de votre PC** :

   ```powershell
   ipconfig
   ```

   Notez l'IPv4 de votre connexion réseau (ex: 192.168.1.10)

6. **Modifier l'URL de l'API** dans `MedVerifyApp/src/config/constants.ts` :

   ```typescript
   export const API_BASE_URL = 'http://192.168.1.10:8080/api/v1';
   ```

7. **Lancer l'application** :
   ```powershell
   npm run android
   ```

## 🔄 Processus de Lancement

Quand vous lancez `npm run android`, voici ce qui se passe :

1. **Metro Bundler** démarre (serveur de développement JavaScript)
2. L'application est **compilée** (Gradle)
3. L'**APK est installé** sur l'émulateur/appareil
4. L'**application se lance** automatiquement

⏱️ **Temps d'attente** :

- Premier lancement : 3-5 minutes (compilation Gradle)
- Lancements suivants : 30 secondes - 1 minute

## ✅ Vérifications

### 1. Metro Bundler est lancé

Vous devriez voir dans le terminal :

```
Welcome to Metro v0.XX.X
  Fast - Scalable - Integrated

Running Metro Bundler on port 8081
```

### 2. L'app est installée

```
BUILD SUCCESSFUL
Installing the app...
```

### 3. L'app se lance

Sur l'émulateur/téléphone, vous devriez voir :

- Écran de splash "MedVerify"
- Puis l'écran de **Login**

## 🧪 Test de l'Application

1. **Écran de Login** :
   - Cliquez sur "S'inscrire" (ou "Registrar" en portugais)

2. **Créer un compte** :
   - Prénom : Test
   - Nom : User
   - Email : test@example.com
   - Téléphone : +245 123456789
   - Mot de passe : Test123456
   - Rôle : Patient
   - Cliquez "S'inscrire"

3. **Se connecter** :
   - Ou utilisez le compte admin :
     - Email : `admin@medverify.gw`
     - Mot de passe : `Admin@123456`
   - Cliquez "Se connecter"

4. **Écran d'accueil** :
   - Vous devriez voir : "Welcome, [Votre nom]!"
   - Bouton "Scan Medication" (fonctionnalité complète)
   - Bouton "Déconnexion"

## 🐛 Dépannage

### Erreur : "Could not connect to development server"

```powershell
# Arrêtez Metro et relancez avec reset cache
npm start -- --reset-cache
```

### Erreur : "SDK location not found"

Créez le fichier `android/local.properties` :

```properties
sdk.dir=C:\\Users\\epifa\\AppData\\Local\\Android\\Sdk
```

### Erreur : "INSTALL_FAILED_INSUFFICIENT_STORAGE"

Libérez de l'espace sur l'émulateur ou utilisez un appareil avec plus d'espace.

### L'app ne se connecte pas à l'API

1. Vérifiez que le backend tourne : http://localhost:8080/actuator/health
2. Pour émulateur : utilisez `http://10.0.2.2:8080/api/v1`
3. Pour appareil physique : utilisez l'IP de votre PC

## 📱 Raccourcis Utiles

Une fois l'app lancée :

- **Reload** : Appuyez 2 fois sur R dans le terminal Metro
- **Dev Menu** : Secouez l'appareil ou Ctrl+M (émulateur)
- **Logs** : Le terminal affiche les logs React Native

## 🎉 Prochaines Étapes

Une fois l'app lancée :

1. Testez la connexion/inscription
2. Testez le scanner (sur appareil physique uniquement - l'émulateur n'a pas de caméra)
3. Générez un QR Code de test avec les données GS1

## 🔗 URLs Importantes

- **Backend Health** : http://localhost:8080/actuator/health
- **Swagger API** : http://localhost:8080/swagger-ui.html
- **Metro Bundler** : http://localhost:8081

---

**Besoin d'aide ?** Consultez les logs du terminal pour les messages d'erreur spécifiques.

