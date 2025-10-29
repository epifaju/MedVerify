# 🔧 FIX ERREUR MAVEN - Windows PowerShell

## ❌ ERREUR RENCONTRÉE

```
mvn spring-boot:run -Dspring-boot.run.profiles=local

[ERROR] Unknown lifecycle phase ".run.profiles=local"
```

**Cause** : Windows PowerShell interprète mal l'argument Maven avec le tiret `-D`.

---

## ✅ SOLUTIONS

### Solution 1 : Variable d'Environnement (PLUS SIMPLE) ⭐

```powershell
# Dans medverify-backend
cd medverify-backend

# Définir le profil
$env:SPRING_PROFILES_ACTIVE="local"

# Lancer
./mvnw spring-boot:run
```

**✅ Avantages** :

- Simple
- Pas de problème de syntaxe
- Fonctionne à tous les coups

---

### Solution 2 : Guillemets (Alternative)

```powershell
cd medverify-backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

**⚠️ IMPORTANT** : Les guillemets sont **obligatoires** sur Windows !

---

### Solution 3 : Script PowerShell Dédié

**Créer** : `medverify-backend/start-local.ps1`

```powershell
# Script de lancement avec profil local
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run
```

**Usage** :

```powershell
cd medverify-backend
./start-local.ps1
```

---

## 🎯 COMMANDE RECOMMANDÉE

### Pour Développement Quotidien

```powershell
# 1. Aller dans le dossier backend
cd medverify-backend

# 2. Définir le profil local
$env:SPRING_PROFILES_ACTIVE="local"

# 3. Lancer
./mvnw spring-boot:run
```

---

## 🔍 VÉRIFICATION

### Logs Attendus au Démarrage

```
INFO - The following profiles are active: local  ← ✅ Profil OK
INFO - Started MedVerifyApplication in X seconds
INFO - Email configuration loaded from: application-local.yml
```

### Si Vous Voyez

```
WARN - Email service not configured
```

**Solution** : Vérifier que `application-local.yml` existe et contient vos credentials.

---

## 📝 FICHIERS NÉCESSAIRES

### ✅ Vérifier que vous avez

```
medverify-backend/
├── src/main/resources/
│   ├── application.yml ✅
│   └── application-local.yml ✅ (avec vos credentials)
└── mvnw ou mvnw.cmd ✅
```

### Si application-local.yml n'existe pas

```powershell
cd medverify-backend/src/main/resources
cp application.yml.example application-local.yml
# Éditer application-local.yml avec vos credentials
```

---

## 🚀 ALTERNATIVE : Lancer Sans Profil (Moins Sécurisé)

### Option A : Credentials en Variables d'Environnement

```powershell
cd medverify-backend

# Définir toutes les variables
$env:SMTP_USERNAME="votre-email@gmail.com"
$env:SMTP_PASSWORD="votre-mot-de-passe"
$env:JWT_SECRET="votre-secret-256-bits"

# Lancer
./mvnw spring-boot:run
```

### Option B : Utiliser application.yml Directement (⚠️ Risqué)

**Non recommandé** : Mettre les credentials dans `application.yml` directement.

**Risque** : Push accidentel sur GitHub !

---

## 🎯 COMPARAISON DES MÉTHODES

| Méthode                             | Sécurité   | Simplicité | Windows |
| ----------------------------------- | ---------- | ---------- | ------- |
| **Variable SPRING_PROFILES_ACTIVE** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅      |
| **Guillemets sur -D**               | ⭐⭐⭐⭐⭐ | ⭐⭐⭐     | ✅      |
| **Script PowerShell**               | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐   | ✅      |
| **Variables env directes**          | ⭐⭐⭐⭐   | ⭐⭐⭐     | ✅      |

**Recommandation** : **Variable SPRING_PROFILES_ACTIVE**

---

## 📊 TROUBLESHOOTING

### Erreur : "mvnw not found"

**Solution** :

```powershell
# Si vous êtes à la racine du projet
cd medverify-backend

# Sur Windows, utiliser
./mvnw.cmd spring-boot:run

# Ou
.\mvnw.cmd spring-boot:run
```

---

### Erreur : "Permission denied"

**Solution** :

```powershell
# Donner les permissions d'exécution
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
./mvnw spring-boot:run
```

---

### Erreur : "Cannot find application-local.yml"

**Solution** :

```powershell
# Vérifier le fichier existe
ls medverify-backend/src/main/resources/application-local.yml

# Si absent, créer depuis template
cp medverify-backend/src/main/resources/application.yml.example medverify-backend/src/main/resources/application-local.yml
```

---

## 🎉 RÉSUMÉ

### ✅ Commande Finale (Windows PowerShell)

```powershell
cd medverify-backend
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run
```

**ou**

```powershell
cd medverify-backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```

---

**Vos credentials sont sécurisés dans `application-local.yml` ! 🔒✅**

**Le backend va démarrer avec le profil local ! 🚀**



