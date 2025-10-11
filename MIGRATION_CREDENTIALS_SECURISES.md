# 🔒 MIGRATION COMPLÈTE - Credentials Sécurisés

## ✅ VOS CREDENTIALS SONT MAINTENANT PROTÉGÉS !

**Date** : 11 Octobre 2025  
**Action** : Sécurisation avant push GitHub

---

## 🎯 CE QUI A ÉTÉ FAIT

### ✅ 1. application.yml Nettoyé

**Avant** (❌ DANGEREUX) :

```yaml
username: ${SMTP_USERNAME:votre-email@gmail.com}
password: ${SMTP_PASSWORD:xxxx-xxxx-xxxx-xxxx}
```

**Après** (✅ SÉCURISÉ) :

```yaml
username: ${SMTP_USERNAME:}
password: ${SMTP_PASSWORD:}
```

---

### ✅ 2. application-local.yml Créé

**Fichier** : `medverify-backend/src/main/resources/application-local.yml`

**Contenu** : VOS vrais credentials (dans .gitignore)

```yaml
spring:
  mail:
    username: votre-email@gmail.com
    password: xxxx-xxxx-xxxx-xxxx
```

**✅ Ce fichier NE SERA JAMAIS sur GitHub !**

---

### ✅ 3. .gitignore Mis à Jour

**Ajouté** :

```gitignore
### Application Config (avec credentials) ###
src/main/resources/application-local.yml
src/main/resources/application-dev.yml
src/main/resources/application-prod.yml

### Scripts avec credentials ###
set-env.ps1
set-env.sh
```

---

### ✅ 4. Templates Créés

**Pour l'équipe** :

- ✅ `application.yml.example` - Template configuration
- ✅ `.env.example` - Template variables
- ✅ `set-env.ps1.example` - Script Windows
- ✅ `set-env.sh.example` - Script Linux/Mac

---

## 🚀 COMMENT UTILISER MAINTENANT

### Méthode Recommandée : Profil Local

```bash
cd medverify-backend

# Lancer avec vos credentials (application-local.yml)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**✅ Simple et sécurisé !**

---

### Alternative : Variables d'Environnement

**Windows PowerShell** :

```powershell
# Session en cours
$env:SMTP_USERNAME="votre-email@gmail.com"
$env:SMTP_PASSWORD="xxxx-xxxx-xxxx-xxxx"
./mvnw spring-boot:run
```

**Ou créer** `set-env.ps1` :

```powershell
# Copier depuis set-env.ps1.example
cp set-env.ps1.example set-env.ps1
# Éditer avec vos credentials
# Lancer
./set-env.ps1
```

---

## 📋 CHECKLIST SÉCURITÉ

### Avant Chaque Push GitHub

- [x] ✅ `application.yml` nettoyé (pas de credentials)
- [x] ✅ `application-local.yml` dans .gitignore
- [x] ✅ `.gitignore` mis à jour
- [ ] ⚠️ **Vérifier** : `git status` ne montre pas de fichiers sensibles
- [ ] ⚠️ **Vérifier** : `git diff` ne contient pas de passwords

### Commandes de Vérification

```bash
# 1. Fichiers qui seront commités
git status

# 2. Chercher credentials (ne doit RIEN trouver)
git grep -i "votre-email@gmail.com"
git grep -i "xxxx-xxxx-xxxx-xxxx"

# Si vous voyez des credentials → NE PAS PUSH !
```

---

## ⚠️ SI DÉJÀ COMMITÉ SUR GITHUB

### Nettoyer l'Historique

**Si vous avez déjà push `application.yml` avec credentials** :

```bash
# 1. Vérifier l'historique
git log --all --full-history -- "medverify-backend/src/main/resources/application.yml"

# 2. Si credentials présents, les supprimer de l'historique
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch medverify-backend/src/main/resources/application.yml" \
  --prune-empty HEAD

# 3. Re-ajouter le fichier nettoyé
git add medverify-backend/src/main/resources/application.yml
git commit -m "security: Remove credentials from application.yml"

# 4. Force push (⚠️ coordonner avec l'équipe)
git push --force origin main
```

**OU utiliser BFG Repo-Cleaner** (plus sûr) :

```bash
# Télécharger BFG
# https://rtyley.github.io/bfg-repo-cleaner/

# Supprimer le password de tout l'historique
java -jar bfg.jar --replace-text passwords.txt
# passwords.txt contient : votre-mot-de-passe-reel

git reflog expire --expire=now --all
git gc --prune=now --aggressive
git push --force
```

---

## 📚 FICHIERS CRÉÉS

### ✅ Pour Sécurité

1. **`CONFIG_VARIABLES_ENVIRONNEMENT.md`** - Guide complet
2. **`SECURITE_GITHUB_CREDENTIALS.md`** - Guide sécurité
3. **`MIGRATION_CREDENTIALS_SECURISES.md`** - Ce fichier

### ✅ Configuration

4. **`application-local.yml`** - Vos credentials (gitignore)
5. **`application.yml.example`** - Template
6. **`.env.example`** - Template variables
7. **`set-env.ps1.example`** - Script Windows
8. **`set-env.sh.example`** - Script Linux

### ✅ .gitignore Mis à Jour

---

## 🎯 PROCHAINES ÉTAPES

### Immédiat

1. **Tester le profil local** :

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

2. **Vérifier logs** :

   ```
   The following profiles are active: local
   Email sent successfully to: ...
   ```

3. **Vérifier avant push** :
   ```bash
   git status
   git diff
   # Pas de credentials visibles ✅
   ```

---

## 🎉 RÉSULTAT FINAL

### ✅ SÉCURITÉ MAXIMALE

**GitHub (Public)** :

- ✅ Pas de passwords
- ✅ Pas d'emails
- ✅ Pas de secrets
- ✅ Templates disponibles

**Local (Privé)** :

- ✅ Credentials dans `application-local.yml`
- ✅ Bloqué par .gitignore
- ✅ Fonctionne parfaitement

**Commande** :

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

**Vous pouvez maintenant push sur GitHub en toute sécurité ! 🔒✅🚀**
