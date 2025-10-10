# 🚀 Lancer l'Application Mobile - MAINTENANT !

## ✅ État Actuel

- ✅ Backend Spring Boot : **ACTIF** (port 8080)
- ✅ Expo Metro : **EN COURS** (arrière-plan)
- ✅ IP configurée : **192.168.1.16**

---

## 📱 ÉTAPE 1 : Ouvrir un Nouveau Terminal

**Ouvrez un NOUVEAU terminal PowerShell** (laissez l'ancien ouvert) et tapez :

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp\MedVerifyExpo
npx expo start
```

Cela va afficher le **QR code** et l'URL.

---

## 📲 ÉTAPE 2 : Scanner le QR Code

### Option A : Via QR Code (Recommandé)

1. **Sur votre téléphone**, ouvrez **Expo Go**
2. Appuyez sur **"Scan QR code"**
3. Scannez le QR code affiché dans le terminal
4. L'application se charge ! 🎉

### Option B : Via URL Manuelle

Si le QR code ne s'affiche pas, cherchez dans le terminal :

```
Metro waiting on exp://192.168.1.16:8081
```

Dans **Expo Go** :

1. Onglet **"Projects"**
2. Tapez : `exp://192.168.1.16:8081`

---

## 🧪 ÉTAPE 3 : Tester l'Application

### Test 1 : Connexion Backend

Sur l'écran de l'app, appuyez sur :

```
🔍 Tester le Backend
```

**Résultat attendu** :

- ✅ Alert : "✅ Succès - Backend accessible !"

### Test 2 : Authentification

**Credentials de test** (affichés dans l'app) :

- Email : `admin@medverify.gw`
- Password : `Admin@123456`

Appuyez sur **"Se connecter"**

**Résultat attendu** :

- ✅ Alert : "Connexion réussie ! Bienvenue Admin !"
- ✅ L'écran change pour "Connecté !"
- ✅ Affiche votre nom d'utilisateur

---

## ❌ Si Expo Ne Démarre Pas

### Solution 1 : Vérifier le Processus

Dans le **premier terminal**, appuyez sur **Ctrl+C** pour arrêter le processus en arrière-plan.

Puis relancez dans un **nouveau terminal** :

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp\MedVerifyExpo
npx expo start
```

### Solution 2 : Clear Cache

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp\MedVerifyExpo
npx expo start --clear
```

---

## 🎯 CE QUE VOUS ALLEZ VOIR

### Sur le Terminal

```
Starting Metro Bundler
› Metro waiting on exp://192.168.1.16:8081
› Scan the QR code above with Expo Go (Android) or the Camera app (iOS)

█████████████████████████████
█████████████████████████████
████ ▄▄▄▄▄ █▀█ █▄█▄█ ▄▄▄▄▄ ████
...

› Press s │ switch to development build
› Press a │ open Android
› Press w │ open web
```

### Sur l'Application Mobile

**Écran 1 : Login**

- Logo 💊 MedVerify
- Bouton vert "🔍 Tester le Backend"
- Champs Email/Password
- Bouton bleu "Se connecter"
- Encadré jaune avec IP/Config
- Encadré bleu avec credentials de test

**Écran 2 : Connecté** (après login)

- ✅ Grande icône
- "Connecté !"
- "Bienvenue [Votre Nom]"
- Message de confirmation
- Bouton rouge "Se déconnecter"

---

## 🔧 Dépannage Rapide

### Erreur : "Network request failed"

1. **Vérifiez le backend** :

   ```
   http://localhost:8080/actuator/health
   ```

   Doit retourner `{"status":"UP"}`

2. **Vérifiez l'IP** :
   Dans `App.tsx` ligne 14, elle doit être : `192.168.1.16`

3. **Même WiFi** :
   PC et téléphone doivent être sur le **même réseau WiFi**

### Expo ne trouve pas le module

Si erreur `module expo is not installed` :

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp\MedVerifyExpo
npm install
npx expo start
```

---

## ✅ CHECKLIST

- [ ] Nouveau terminal ouvert
- [ ] `npx expo start` lancé
- [ ] QR code visible
- [ ] Expo Go installé sur téléphone
- [ ] QR code scanné
- [ ] App chargée
- [ ] Test backend → ✅ Succès
- [ ] Login → ✅ Connecté

---

## 🎊 SUCCÈS !

Si vous arrivez à vous connecter, **FÉLICITATIONS** ! 🎉

Vous avez :

- ✅ Une app mobile React Native / Expo
- ✅ Connectée à un backend Spring Boot
- ✅ Avec authentification JWT fonctionnelle
- ✅ Communication client-serveur validée

---

**Maintenant, ouvrez le nouveau terminal et lancez Expo !** 🚀
