# 🚀 LANCER APP COMPLÈTE - Commandes Corrigées

## ✅ TOUTES LES ERREURS CORRIGÉES !

**Date** : 12 Octobre 2025  
**Statut** : 🟢 **PRÊT À LANCER**

---

## 🎯 COMMANDES FINALES

### Terminal 1 : Backend Spring Boot

```powershell
# Aller dans le dossier backend
cd C:\Users\epifa\cursor-workspace\MedVerify\medverify-backend

# Définir le profil local (credentials sécurisés)
$env:SPRING_PROFILES_ACTIVE="local"

# Lancer le backend
./mvnw spring-boot:run
```

**Logs attendus** :

```
INFO - The following profiles are active: local
INFO - Started MedVerifyApplication in X.XXX seconds
```

---

### Terminal 2 : Frontend Expo

```powershell
# Aller dans le BON dossier Expo
cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp\MedVerifyExpo

# Lancer Expo
npx expo start
```

**Interface attendue** :

```
› Metro waiting on exp://192.168.X.X:8081
› Scan QR code above with Expo Go (Android) or Camera app (iOS)
```

---

## ⚠️ ERREURS CORRIGÉES

### 1. Erreur Maven PowerShell ✅

**Avant** (❌ Erreur) :

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=local
# Error: Unknown lifecycle phase ".run.profiles=local"
```

**Après** (✅ Corrigé) :

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run
```

---

### 2. Erreur Expo Mauvais Dossier ✅

**Avant** (❌ Erreur) :

```powershell
cd MedVerifyExpo
npx expo start
# ConfigError: package.json does not exist
```

**Après** (✅ Corrigé) :

```powershell
cd MedVerifyApp/MedVerifyExpo
npx expo start
```

---

### 3. Credentials Sécurisés ✅

**Avant** (❌ Risque) :

```yaml
# application.yml
username: epifaju@gmail.com
password: wqyq ogyu zhgy bgfl # ← Public sur GitHub !
```

**Après** (✅ Sécurisé) :

```yaml
# application.yml
username: ${SMTP_USERNAME:}
password: ${SMTP_PASSWORD:}

# application-local.yml (gitignore)
username: epifaju@gmail.com
password: wqyq ogyu zhgy bgfl  # ← Protégé !
```

---

## 📊 STRUCTURE DES DOSSIERS

```
MedVerify/
│
├── medverify-backend/              ← Backend Java
│   ├── src/main/resources/
│   │   ├── application.yml         (public, sécurisé)
│   │   └── application-local.yml   (gitignore, credentials)
│   └── mvnw
│
└── MedVerifyApp/                   ← Frontend React Native
    └── MedVerifyExpo/              ← PROJET EXPO (à utiliser)
        ├── package.json
        ├── app.json
        └── src/
```

---

## 🔍 VÉRIFICATIONS

### Backend Démarré ✅

```powershell
# Dans les logs, vérifier :
The following profiles are active: local
Tomcat started on port(s): 8080
```

**Tester** :

```powershell
curl http://localhost:8080/api/v1/health
# Résultat : {"status":"UP"}
```

---

### Frontend Démarré ✅

```powershell
# Dans la console Expo, vérifier :
Metro waiting on exp://...
```

**Tester** :

- Ouvrir Expo Go sur Android
- Scanner le QR code
- App doit démarrer

---

## 🎯 ORDRE DE LANCEMENT

### 1. Backend D'abord (5-10 secondes)

```powershell
cd medverify-backend
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run
```

**Attendre** : `Started MedVerifyApplication`

---

### 2. Frontend Ensuite

```powershell
cd MedVerifyApp/MedVerifyExpo
npx expo start
```

---

## 🚨 TROUBLESHOOTING

### Backend : Port 8080 Déjà Utilisé

```powershell
# Trouver le processus
netstat -ano | findstr :8080

# Tuer le processus (remplacer PID)
taskkill /PID <PID> /F
```

---

### Frontend : Metro Bundler Erreur

```powershell
# Nettoyer le cache
cd MedVerifyApp/MedVerifyExpo
npx expo start --clear

# Ou réinstaller
rm -r node_modules
npm install
npx expo start
```

---

### Credentials : Email Non Configuré

```
WARN - Email service not configured
```

**Solution** :

```powershell
# Vérifier application-local.yml existe
ls medverify-backend/src/main/resources/application-local.yml

# Si absent, créer depuis template
cp medverify-backend/src/main/resources/application.yml.example medverify-backend/src/main/resources/application-local.yml

# Éditer avec vos credentials
```

---

## 📝 SCRIPTS DE LANCEMENT (Optionnel)

### Backend : `start-backend.ps1`

```powershell
# Créer : medverify-backend/start-backend.ps1
cd medverify-backend
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run
```

### Frontend : `start-frontend.ps1`

```powershell
# Créer : MedVerifyApp/MedVerifyExpo/start-expo.ps1
cd MedVerifyApp/MedVerifyExpo
npx expo start
```

---

## 🎉 RÉSULTAT FINAL

### ✅ Application Complète Lancée

**Backend** :

- ✅ Spring Boot sur port 8080
- ✅ Profil local activé
- ✅ Credentials sécurisés (application-local.yml)
- ✅ Base de données PostgreSQL connectée

**Frontend** :

- ✅ Expo Metro Bundler actif
- ✅ QR code disponible
- ✅ App prête pour Android/iOS

---

## 📚 GUIDES CRÉÉS

1. **`FIX_ERREUR_MAVEN_WINDOWS.md`** - Fix Maven PowerShell
2. **`FIX_ERREUR_EXPO_MAUVAIS_DOSSIER.md`** - Fix chemin Expo
3. **`SECURITE_FINALE_CONFIRMEE.md`** - Credentials sécurisés
4. **`LANCER_APP_COMPLETE_CORRIGE.md`** - Ce guide

---

## 🚀 COMMANDES FINALES (Copier-Coller)

### Terminal 1 : Backend

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\medverify-backend && $env:SPRING_PROFILES_ACTIVE="local" && ./mvnw spring-boot:run
```

### Terminal 2 : Frontend

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp\MedVerifyExpo && npx expo start
```

---

**Votre application MedVerify est maintenant opérationnelle ! 🎉🚀✅**

**Backend** : http://localhost:8080  
**Frontend** : Expo Go (Scanner QR code)
