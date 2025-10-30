# 🔄 Relancer l'Application Correctement

## Problème

L'application n'a pas été rechargée après les changements de configuration.

## Solution Immédiate

### Option 1: Reload Hard

Dans l'application mobile:
1. Appuyez sur **`r`** dans le terminal Metro
2. OU faites un **Shake gesture** (secouer le téléphone)
3. Sélectionnez **"Reload"**

### Option 2: Stop et Restart Complet

**Terminal Metro:**
1. Appuyez sur **`Ctrl+C`** pour arrêter Metro
2. Exécutez:
```bash
cd MedVerifyApp/MedVerifyExpo
npm start
```
3. Appuyez sur **`a`** pour Android

### Option 3: Clear Cache

**Terminal Metro:**
1. Stop Metro (`Ctrl+C`)
2. Exécutez:
```bash
cd MedVerifyApp/MedVerifyExpo
npm start -- --clear
```
3. Appuyez sur **`a`** pour Android

---

## Vérifications Après Reload

### 1. Vérifier l'URL dans les logs

Dans les logs Metro, recherchez:
```
LOG  🌐 API Base URL: http://localhost:8080/api/v1
```

Si vous voyez `192.168.1.16`, l'app n'a pas été rechargée correctement.

### 2. Tester Debug Screen

1. Allez sur l'écran Login
2. Appuyez sur "🔧 Debug Réseau"
3. Appuyez sur "Lancer les tests"
4. Les tests doivent maintenant PASSER ✅

### 3. Essayer de se Connecter

Tentez de vous connecter avec vos credentials.

---

## Si Toujours Pas de Résultat

### Vérifier ADB Reverse

```bash
adb reverse --list
```

Doit afficher:
```
UsbFfs tcp:8081 tcp:8081
UsbFfs tcp:8080 tcp:8080
```

### Redéfinir ADB Reverse

```bash
# Supprimer
adb reverse --remove tcp:8080

# Re-créer
adb reverse tcp:8080 tcp:8080

# Vérifier
adb reverse --list
```

### Backend Tourne?

```bash
curl http://localhost:8080/actuator/health
```

Doit retourner: `{"status":"UP"}`

---

## Diagnostic Complet

Si ABSOLUMENT rien ne fonctionne:

1. **Backend OK?** → `curl localhost:8080/actuator/health`
2. **ADB OK?** → `adb reverse --list`
3. **App rechargée?** → Vérifier logs Metro
4. **USB connecté?** → `adb devices`

---

## Commande Ultimate Fix

```bash
# Terminal 1: Backend
cd medverify-backend
mvn spring-boot:run

# Terminal 2: Setup ADB
adb reverse --remove-all
adb reverse tcp:8080 tcp:8080
adb reverse --list

# Terminal 2 (suite): Expo
cd MedVerifyApp/MedVerifyExpo
npm start -- --clear

# Appuyez 'a' pour Android
# Puis appuyez 'r' dans l'app pour reload
```

---

**ESSENTIEL: Reload l'application après chaque changement!**

