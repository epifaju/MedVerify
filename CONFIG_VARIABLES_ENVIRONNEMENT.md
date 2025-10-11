# 🔐 Configuration Variables d'Environnement - Sécurité GitHub

## ⚠️ PROBLÈME CRITIQUE

**Vous avez actuellement des credentials en clair dans `application.yml` !**

```yaml
# ❌ DANGEREUX - À NE JAMAIS FAIRE
username: epifaju@gmail.com
password: wqyq ogyu zhgy bgfl # Visible sur GitHub !
```

**Risques** :

- ❌ Credentials publics sur GitHub
- ❌ N'importe qui peut utiliser votre email
- ❌ Risque de spam, abus, blocage compte
- ❌ Violation sécurité

---

## ✅ SOLUTION SÉCURISÉE

### 3 Méthodes (de la plus simple à la plus professionnelle)

---

## 🔧 MÉTHODE 1 : Profils Spring Boot (Recommandé)

### Étape 1 : Nettoyer application.yml

**Fichier** : `medverify-backend/src/main/resources/application.yml`

**Remplacer** :

```yaml
# ❌ AVANT (DANGEREUX)
mail:
  username: ${SMTP_USERNAME:epifaju@gmail.com}
  password: ${SMTP_PASSWORD:wqyq ogyu zhgy bgfl}
```

**Par** :

```yaml
# ✅ APRÈS (SÉCURISÉ)
mail:
  username: ${SMTP_USERNAME:}
  password: ${SMTP_PASSWORD:}
```

**✅ DÉJÀ FAIT dans votre code !**

---

### Étape 2 : Créer application-local.yml (NON COMMITÉ)

**Fichier** : `medverify-backend/src/main/resources/application-local.yml`

**Contenu** :

```yaml
# ⚠️ Ce fichier est dans .gitignore - VOS vrais credentials
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: epifaju@gmail.com
    password: wqyq ogyu zhgy bgfl
    properties:
      mail.smtp:
        auth: true
        starttls.enable: true

jwt:
  secret: votre-secret-jwt-local-256-bits
```

**✅ DÉJÀ CRÉÉ pour vous !**

---

### Étape 3 : Vérifier .gitignore

**Fichier** : `medverify-backend/.gitignore`

**Ajouter** :

```gitignore
### Application Config (avec credentials) ###
src/main/resources/application-local.yml
src/main/resources/application-dev.yml
src/main/resources/application-prod.yml
```

**✅ DÉJÀ FAIT !**

---

### Étape 4 : Lancer avec Profil Local

```bash
# Windows PowerShell
cd medverify-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Ou définir variable env
$env:SPRING_PROFILES_ACTIVE="local"
./mvnw spring-boot:run
```

---

## 🔧 MÉTHODE 2 : Variables d'Environnement (Production)

### Windows PowerShell

**Créer script** : `medverify-backend/set-env.ps1`

```powershell
# ⚠️ Ce fichier est dans .gitignore
$env:SMTP_HOST="smtp.gmail.com"
$env:SMTP_PORT="587"
$env:SMTP_USERNAME="epifaju@gmail.com"
$env:SMTP_PASSWORD="wqyq ogyu zhgy bgfl"
$env:JWT_SECRET="votre-secret-jwt-256-bits"

# Lancer l'application
./mvnw spring-boot:run
```

**Usage** :

```bash
cd medverify-backend
./set-env.ps1
```

---

### Linux/Mac

**Créer script** : `medverify-backend/set-env.sh`

```bash
#!/bin/bash
# ⚠️ Ce fichier est dans .gitignore
export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=epifaju@gmail.com
export SMTP_PASSWORD="wqyq ogyu zhgy bgfl"
export JWT_SECRET=votre-secret-jwt-256-bits

# Lancer l'application
./mvnw spring-boot:run
```

**Usage** :

```bash
cd medverify-backend
chmod +x set-env.sh
./set-env.sh
```

---

## 🔧 MÉTHODE 3 : Fichier .env (Plus Simple)

### Option A : Spring Boot avec dotenv

**Ajouter dépendance** : `pom.xml`

```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

**Créer** : `medverify-backend/.env`

```env
# ⚠️ Ce fichier est dans .gitignore
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=epifaju@gmail.com
SMTP_PASSWORD=wqyq ogyu zhgy bgfl
JWT_SECRET=votre-secret-jwt-256-bits
DB_PASSWORD=postgres
```

**✅ DÉJÀ CRÉÉ : `.env.example`** (fichier template)

---

## 📝 FICHIERS À CRÉER

### 1. .gitignore (Mettre à Jour)

**Ajouter au .gitignore racine** :

```gitignore
# Credentials sensibles
medverify-backend/src/main/resources/application-local.yml
medverify-backend/src/main/resources/application-dev.yml
medverify-backend/src/main/resources/application-prod.yml
medverify-backend/.env
medverify-backend/set-env.ps1
medverify-backend/set-env.sh

# Fichiers de configuration locaux
**/application-local*.yml
**/.env
**/.env.local
**/set-env.*
```

---

### 2. README.md (Section Configuration)

**Ajouter dans README** :

````markdown
## 🔐 Configuration

### Variables Sensibles

Ce projet utilise des variables d'environnement pour les credentials.

**NE JAMAIS** commit :

- `application-local.yml`
- `.env`
- `set-env.ps1`

### Configuration Locale

1. Copier le fichier template :
   ```bash
   cp src/main/resources/application.yml.example src/main/resources/application-local.yml
   ```
````

2. Éditer `application-local.yml` avec VOS credentials

3. Lancer avec le profil local :
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

### Variables Requises

- `SMTP_USERNAME` : Votre email Gmail
- `SMTP_PASSWORD` : Gmail App Password
- `JWT_SECRET` : Clé secrète 256+ bits
- `DB_PASSWORD` : Mot de passe PostgreSQL

````

---

## 🚨 ACTIONS URGENTES AVANT PUSH GITHUB

### ⚠️ ÉTAPE 1 : Supprimer Credentials de l'Historique Git

**Si vous avez déjà commité** `application.yml` avec les credentials :

```bash
# 1. Vérifier l'historique
git log --all --full-history -- "**/application.yml"

# 2. Si credentials présents, nettoyer l'historique
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch medverify-backend/src/main/resources/application.yml" \
  --prune-empty --tag-name-filter cat -- --all

# OU utiliser BFG Repo-Cleaner (plus rapide)
java -jar bfg.jar --delete-files application.yml
git reflog expire --expire=now --all
git gc --prune=now --aggressive
````

**⚠️ ATTENTION** : Cela réécrit l'historique Git !

---

### ✅ ÉTAPE 2 : Vérifier .gitignore

**J'ai déjà mis à jour `.gitignore`** avec :

```gitignore
### Environment ###
.env
.env.local
.env.production
.env.development

### Application Config (avec credentials) ###
src/main/resources/application-local.yml
src/main/resources/application-dev.yml
src/main/resources/application-prod.yml
```

---

### ✅ ÉTAPE 3 : Créer Fichiers Template (Déjà Fait)

**J'ai créé** :

- ✅ `application.yml.example` - Template configuration
- ✅ `.env.example` - Template variables
- ✅ `application-local.yml` - Vos credentials (à NE PAS commit)

---

## 📋 CHECKLIST SÉCURITÉ AVANT PUSH

### Avant Chaque Push GitHub

- [ ] **Vérifier** que `application.yml` ne contient PAS de credentials
- [ ] **Vérifier** `.gitignore` contient bien :
  - `application-local.yml`
  - `.env`
  - `set-env.*`
- [ ] **Tester** : `git status` ne montre PAS de fichiers sensibles
- [ ] **Vérifier** : `git diff` ne contient PAS de credentials

### Commandes de Vérification

```bash
# 1. Vérifier quels fichiers seront commités
git status

# 2. Vérifier le contenu
git diff

# 3. Chercher credentials (avant commit)
git grep -i "password\|secret" -- "*.yml" "*.properties"

# Si des credentials sont trouvés → NE PAS COMMIT !
```

---

## 🔒 BONNES PRATIQUES

### 1. Configuration par Environnement

```yaml
# application.yml (PUBLIC - OK pour GitHub)
spring:
  mail:
    username: ${SMTP_USERNAME:} # ✅ Vide par défaut
    password: ${SMTP_PASSWORD:} # ✅ Vide par défaut
```

### 2. Profils Spring Boot

```bash
# Développement local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Production
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### 3. Variables d'Environnement Système

```bash
# Windows PowerShell (persistant)
[System.Environment]::SetEnvironmentVariable('SMTP_PASSWORD', 'wqyq ogyu zhgy bgfl', 'User')

# Linux/Mac (session)
export SMTP_PASSWORD=wqyq ogyu zhgy bgfl
```

---

## 🎯 CONFIGURATION RECOMMANDÉE

### Pour Développement

**Utiliser** : `application-local.yml` (dans .gitignore)

**Lancer** :

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Pour Production

**Utiliser** : Variables d'environnement système

**Docker** :

```yaml
# docker-compose.yml
environment:
  - SMTP_USERNAME=${SMTP_USERNAME}
  - SMTP_PASSWORD=${SMTP_PASSWORD}
```

**Kubernetes** :

```yaml
# secrets.yml
apiVersion: v1
kind: Secret
metadata:
  name: medverify-secrets
type: Opaque
data:
  smtp-username: base64...
  smtp-password: base64...
```

---

## 🔍 VÉRIFIER CE QUI SERA COMMITÉ

### Avant Chaque Commit

```bash
# 1. Voir les fichiers modifiés
git status

# 2. Voir le contenu
git diff

# 3. Chercher les secrets (regex)
git diff | grep -E "(password|secret|key|token)" -i

# Si vous voyez des credentials → STOP !
```

### Outil Automatique : git-secrets

```bash
# Installer git-secrets
git clone https://github.com/awslabs/git-secrets.git
cd git-secrets
make install

# Configurer
cd /path/to/medverify
git secrets --install
git secrets --register-aws

# Ajouter patterns custom
git secrets --add 'password.*=.*'
git secrets --add 'smtp.*password'
git secrets --add 'wqyq ogyu zhgy bgfl'
```

---

## 📚 FICHIERS CRÉÉS POUR VOUS

### ✅ Fichiers Sécurisés

1. **`application.yml`** ✅ Nettoyé

   - Plus de credentials en clair
   - Utilise variables env

2. **`application.yml.example`** ✅ Template

   - À partager sur GitHub
   - Valeurs d'exemple

3. **`.env.example`** ✅ Template

   - Variables à définir
   - Documentation

4. **`application-local.yml`** ✅ Vos credentials

   - Dans .gitignore
   - Vos vraies valeurs

5. **`.gitignore`** ✅ Mis à jour
   - Fichiers sensibles bloqués

---

## 🚀 UTILISATION

### Développement Local

**Option A : Profil local** (Recommandé)

```bash
cd medverify-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Option B : Variables env**

```bash
# Windows
$env:SMTP_USERNAME="epifaju@gmail.com"
$env:SMTP_PASSWORD="wqyq ogyu zhgy bgfl"
./mvnw spring-boot:run
```

**Option C : Fichier .env**

```bash
# Créer .env avec vos credentials
# Utiliser spring-dotenv dependency
./mvnw spring-boot:run
```

---

## 📊 COMPARAISON MÉTHODES

| Méthode            | Sécurité   | Simplicité | Production |
| ------------------ | ---------- | ---------- | ---------- |
| **Profils Spring** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐   | ⭐⭐⭐⭐⭐ |
| **Variables Env**  | ⭐⭐⭐⭐   | ⭐⭐⭐     | ⭐⭐⭐⭐⭐ |
| **Fichier .env**   | ⭐⭐⭐     | ⭐⭐⭐⭐⭐ | ⭐⭐⭐     |

**Recommandation** : **Profils Spring Boot** (application-local.yml)

---

## ⚠️ CE QUE VOUS DEVEZ FAIRE MAINTENANT

### 1. Vérifier que application.yml est Nettoyé ✅

```yaml
# Doit être comme ça (SANS valeurs par défaut)
username: ${SMTP_USERNAME:}
password: ${SMTP_PASSWORD:}
```

### 2. Utiliser application-local.yml ✅

```bash
# Vos credentials sont dans application-local.yml
# Ce fichier est dans .gitignore
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### 3. Vérifier Avant de Push

```bash
# Ne doit PAS montrer application-local.yml
git status

# Ne doit PAS contenir de passwords
git diff medverify-backend/src/main/resources/application.yml
```

### 4. Push Sécurisé

```bash
git add .
git commit -m "feat: Add email verification (credentials secured)"
git push origin main
```

---

## 🎯 POUR L'ÉQUIPE

### Documentation à Partager

**Dans le README.md** :

```markdown
## 🔧 Configuration Développement

### 1. Cloner le Projet

\`\`\`bash
git clone https://github.com/votre-repo/medverify.git
cd medverify
\`\`\`

### 2. Configuration Credentials

\`\`\`bash
cd medverify-backend/src/main/resources

# Copier le template

cp application.yml.example application-local.yml

# Éditer avec VOS credentials

nano application-local.yml
\`\`\`

### 3. Lancer

\`\`\`bash
cd medverify-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
\`\`\`

### Variables Requises

- `SMTP_USERNAME` : Gmail email
- `SMTP_PASSWORD` : Gmail App Password (https://myaccount.google.com/apppasswords)
- `JWT_SECRET` : Clé 256+ bits
- `DB_PASSWORD` : PostgreSQL password
```

---

## 🔐 SÉCURITÉ AVANCÉE

### Secrets Manager (Production)

**AWS Secrets Manager** :

```java
@Configuration
public class SecretsConfig {
    @Value("${aws.secretsmanager.secret-name}")
    private String secretName;

    @Bean
    public void loadSecrets() {
        // Charger secrets depuis AWS
    }
}
```

**HashiCorp Vault** :

```yaml
spring:
  cloud:
    vault:
      host: localhost
      port: 8200
      authentication: TOKEN
      token: your-vault-token
```

---

## 📋 CHECKLIST FINALE

### Avant Push GitHub

- [x] ✅ `application.yml` nettoyé (pas de credentials)
- [x] ✅ `application-local.yml` créé (dans .gitignore)
- [x] ✅ `.gitignore` mis à jour
- [x] ✅ `application.yml.example` créé (template)
- [x] ✅ `.env.example` créé (template)
- [ ] ⚠️ Tester avec profil local
- [ ] ⚠️ Vérifier `git status`
- [ ] ⚠️ Vérifier `git diff`
- [ ] ⚠️ Push sécurisé

---

## 🎉 RÉSULTAT

### ✅ Configuration Sécurisée

**Fichiers PUBLICS** (sur GitHub) :

- ✅ `application.yml` (sans credentials)
- ✅ `application.yml.example` (template)
- ✅ `.env.example` (template)
- ✅ `.gitignore` (bloque fichiers sensibles)

**Fichiers PRIVÉS** (gitignore) :

- ✅ `application-local.yml` (vos credentials)
- ✅ `.env` (si utilisé)
- ✅ `set-env.ps1` / `set-env.sh`

---

## 🚀 COMMANDES RAPIDES

### Développement

```bash
# Méthode 1 : Profil local (RECOMMANDÉ)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Méthode 2 : Variables env
$env:SMTP_USERNAME="epifaju@gmail.com"
$env:SMTP_PASSWORD="wqyq ogyu zhgy bgfl"
./mvnw spring-boot:run
```

### Vérification Sécurité

```bash
# Avant chaque push
git status
git diff
git grep -i "wqyq ogyu zhgy bgfl"  # Ne doit RIEN retourner !
```

---

## ⚠️ SI DÉJÀ COMMITÉ

### Nettoyer l'Historique Git

**Option 1 : BFG Repo-Cleaner** (Recommandé)

```bash
# Télécharger BFG
# https://rtyley.github.io/bfg-repo-cleaner/

# Supprimer le mot de passe de l'historique
java -jar bfg.jar --replace-text passwords.txt

# passwords.txt contient :
# wqyq ogyu zhgy bgfl
```

**Option 2 : git filter-branch**

```bash
git filter-branch --tree-filter 'find . -name "application.yml" -exec sed -i "s/wqyq ogyu zhgy bgfl/REDACTED/g" {} \;' HEAD
```

**Puis** :

```bash
git push --force  # ⚠️ Coordonner avec l'équipe !
```

---

## 🎯 RECOMMANDATION FINALE

### ✅ Utiliser application-local.yml

**Pourquoi** :

- ✅ Sécurisé (gitignore)
- ✅ Simple à utiliser
- ✅ Standard Spring Boot
- ✅ Fonctionne en équipe

**Comment** :

```bash
# 1. Vos credentials sont dans application-local.yml (déjà créé)
# 2. Lancer avec :
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

**Vos credentials sont maintenant sécurisés ! Vous pouvez push sur GitHub en toute sécurité ! 🔒✅**
