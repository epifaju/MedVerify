# 🔒 GUIDE SÉCURITÉ - Protéger Credentials sur GitHub

## ⚠️ DANGER ACTUEL

**Votre `application.yml` contenait** :

```yaml
username: votre-email@gmail.com
password: xxxx-xxxx-xxxx-xxxx # ❌ PUBLIC sur GitHub !
```

**Risques** :

- 🚨 N'importe qui peut lire votre mot de passe
- 🚨 Utiliser votre compte email pour spam
- 🚨 Google peut bloquer votre compte
- 🚨 Violation RGPD/sécurité

---

## ✅ SOLUTION IMPLÉMENTÉE

### J'ai Sécurisé Vos Credentials

**1. Nettoyé `application.yml`** ✅

```yaml
# ✅ SÉCURISÉ (plus de credentials en clair)
username: ${SMTP_USERNAME:}
password: ${SMTP_PASSWORD:}
```

**2. Créé `application-local.yml`** ✅

```yaml
# Vos VRAIS credentials (dans .gitignore)
username: votre-email@gmail.com
password: xxxx-xxxx-xxxx-xxxx
```

**3. Mis à jour `.gitignore`** ✅

```gitignore
application-local.yml  # Bloqué sur GitHub
.env
set-env.ps1
```

**4. Créé Templates** ✅

- `application.yml.example`
- `.env.example`
- `set-env.ps1.example`

---

## 🚀 COMMENT UTILISER

### Développement Local

**Lancer avec profil local** :

```bash
cd medverify-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**✅ Vos credentials dans `application-local.yml` seront utilisés !**

---

## 📋 CHECKLIST AVANT PUSH GITHUB

### ⚠️ IMPORTANT - À FAIRE MAINTENANT

```bash
# 1. Vérifier que application.yml est propre
cat src/main/resources/application.yml | grep -i password
# Résultat attendu : ${SMTP_PASSWORD:}  (PAS de valeur en clair)

# 2. Vérifier .gitignore
cat .gitignore | grep application-local
# Résultat attendu : application-local.yml

# 3. Vérifier status git
git status
# Ne doit PAS montrer : application-local.yml, .env, set-env.ps1

# 4. Si tout est OK → Push
git push origin main
```

---

## 🔍 VÉRIFICATION POST-PUSH

### Sur GitHub

1. Aller sur votre repo GitHub
2. Ouvrir `medverify-backend/src/main/resources/application.yml`
3. **Vérifier** :

   ```yaml
   # ✅ Doit être comme ça
   username: ${SMTP_USERNAME:}
   password: ${SMTP_PASSWORD:}

   # ❌ NE DOIT PAS contenir
   username: votre-email@gmail.com
   password: xxxx-xxxx-xxxx-xxxx
   ```

---

## 🎯 POUR VOTRE ÉQUIPE

### Setup Pour Nouveaux Développeurs

**Documentation README** :

```markdown
## Configuration Locale

1. Cloner le projet
2. Copier le template :
   \`\`\`bash
   cd medverify-backend/src/main/resources
   cp application.yml.example application-local.yml
   \`\`\`
3. Éditer `application-local.yml` avec VOS credentials
4. Lancer :
   \`\`\`bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   \`\`\`
```

---

## 🎉 RÉSULTAT

### ✅ Credentials Sécurisés !

**Sur GitHub** (Public) :

- ✅ `application.yml` (sans credentials)
- ✅ `application.yml.example` (template)
- ✅ `.env.example` (template)

**Sur Votre PC** (Privé) :

- ✅ `application-local.yml` (vos credentials)
- ✅ Bloqué par .gitignore

**Commande** :

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

**Vos credentials sont maintenant protégés ! 🔒✅**

**Vous pouvez push sur GitHub en toute sécurité !** 🚀
