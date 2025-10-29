# 🔧 FIX ERREUR EXPO - Mauvais Dossier

## ❌ ERREUR RENCONTRÉE

```
npx expo start --clear --tunnel
ConfigError: The expected package.json path:
C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyExpo\package.json does not exist
```

**Cause** : Lancement depuis le **mauvais dossier** !

---

## 🔍 ANALYSE DU PROBLÈME

### Structure du Projet MedVerify

Vous avez **DEUX dossiers** avec des noms similaires :

```
MedVerify/
├── MedVerifyExpo/          ❌ Dossier incomplet (sources anciennes)
│   └── src/                   - Pas de package.json !
│                              - Ancien code
│
└── MedVerifyApp/           ✅ Projet principal
    └── MedVerifyExpo/      ✅ BON PROJET EXPO
        ├── package.json        - ✅ Fichier présent
        ├── app.json
        ├── src/
        └── node_modules/
```

**Confusion** :

- `MedVerifyExpo` à la racine = ❌ Ancien dossier incomplet
- `MedVerifyApp/MedVerifyExpo` = ✅ Vrai projet Expo

---

## ✅ SOLUTION

### Commande Correcte

```powershell
# Aller dans le BON dossier
cd MedVerifyApp/MedVerifyExpo

# Lancer Expo
npx expo start --clear
```

**ou depuis la racine** :

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp\MedVerifyExpo
npx expo start
```

---

## 🚀 COMMANDES COMPLÈTES

### Depuis la Racine du Projet

```powershell
# Backend (Terminal 1)
cd medverify-backend
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run

# Frontend (Terminal 2)
cd MedVerifyApp/MedVerifyExpo
npx expo start
```

---

## 🔍 VÉRIFICATION

### Comment Savoir si Vous Êtes dans le Bon Dossier

```powershell
# Lister les fichiers
ls

# ✅ Vous DEVEZ voir :
# - package.json
# - app.json
# - src/
# - node_modules/

# ❌ Si vous ne voyez que src/ → Mauvais dossier !
```

---

## 📁 NETTOYAGE (Optionnel)

### Supprimer l'Ancien Dossier MedVerifyExpo (à la racine)

⚠️ **Attention** : Vérifiez qu'il n'y a rien d'important dedans !

```powershell
# Depuis la racine MedVerify
# Vérifier le contenu
ls MedVerifyExpo

# Si c'est juste des anciennes sources
Remove-Item -Recurse -Force MedVerifyExpo
```

**Recommandation** : Renommer plutôt que supprimer

```powershell
# Renommer pour éviter la confusion
Rename-Item MedVerifyExpo MedVerifyExpo_OLD
```

---

## 🎯 STRUCTURE CLARIFIÉE

### Ce Que Vous Avez Maintenant

```
MedVerify/
│
├── medverify-backend/       ← Backend Spring Boot
│   ├── src/
│   ├── pom.xml
│   └── mvnw
│
└── MedVerifyApp/            ← Frontend React Native
    └── MedVerifyExpo/       ← Projet Expo (À UTILISER)
        ├── package.json     ✅
        ├── app.json
        ├── src/
        │   ├── screens/
        │   ├── services/
        │   └── ...
        └── node_modules/
```

---

## 📊 COMPARAISON

| Dossier                      | Package.json | À Utiliser |
| ---------------------------- | ------------ | ---------- |
| `MedVerifyExpo` (racine)     | ❌ Non       | ❌ NON     |
| `MedVerifyApp/MedVerifyExpo` | ✅ Oui       | ✅ OUI     |

---

## 🔧 TROUBLESHOOTING

### Si Expo Ne Démarre Toujours Pas

```powershell
# 1. Vérifier le dossier
pwd
# Résultat attendu : .../MedVerify/MedVerifyApp/MedVerifyExpo

# 2. Vérifier package.json existe
Test-Path package.json
# Résultat attendu : True

# 3. Réinstaller les dépendances
npm install

# 4. Relancer
npx expo start --clear
```

---

### Si Erreur "Module Not Found"

```powershell
# Nettoyer et réinstaller
rm -r node_modules
rm package-lock.json
npm install
npx expo start --clear
```

---

## ✅ COMMANDES RAPIDES

### Bookmark Ces Commandes

```powershell
# Backend
cd C:\Users\epifa\cursor-workspace\MedVerify\medverify-backend
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run

# Frontend
cd C:\Users\epifa\cursor-workspace\MedVerify\MedVerifyApp\MedVerifyExpo
npx expo start
```

---

## 🎉 RÉSUMÉ

**Problème** : Deux dossiers `MedVerifyExpo` prêtent à confusion

**Solution** :

- ✅ Utiliser `MedVerifyApp/MedVerifyExpo` (avec package.json)
- ❌ Ignorer `MedVerifyExpo` à la racine (ancien dossier)

**Commande** :

```powershell
cd MedVerifyApp/MedVerifyExpo
npx expo start
```

---

**Le bon projet Expo est maintenant lancé ! 🚀✅**

