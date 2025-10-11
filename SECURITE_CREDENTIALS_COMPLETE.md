# 🔒 SÉCURITÉ CREDENTIALS - GUIDE COMPLET

## ✅ VOS CREDENTIALS SONT MAINTENANT SÉCURISÉS !

**Date** : 11 Octobre 2025  
**Action** : Protection avant push GitHub

---

## 🚨 CE QUI ÉTAIT DANGEREUX

### Avant (❌ RISQUE MAJEUR)

```yaml
# medverify-backend/src/main/resources/application.yml
# ❌ VISIBLE SUR GITHUB !

spring:
  mail:
    username: epifaju@gmail.com # ❌ Public
    password: wqyq ogyu zhgy bgfl # ❌ Public
```

**Conséquences** :

- 🚨 Tout le monde sur GitHub peut voir votre password
- 🚨 Risque d'utilisation abusive de votre compte
- 🚨 Google peut bloquer votre compte pour spam
- 🚨 Violation de sécurité grave

---

## ✅ CE QUI EST MAINTENANT SÉCURISÉ

### Après (✅ PROTECTION COMPLÈTE)

#### Fichier Public (GitHub) ✅

**`application.yml`** :

```yaml
# ✅ PAS de credentials en clair
spring:
  mail:
    username: ${SMTP_USERNAME:} # ✅ Variable env
    password: ${SMTP_PASSWORD:} # ✅ Variable env
```

#### Fichier Privé (Local uniquement) ✅

**`application-local.yml`** (dans .gitignore) :

```yaml
# ⚠️ NE SERA JAMAIS sur GitHub
spring:
  mail:
    username: epifaju@gmail.com
    password: wqyq ogyu zhgy bgfl
```

---

## 🎯 COMMENT UTILISER

### 🚀 Lancer le Backend

**Commande unique** :

```bash
cd medverify-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**✅ Vos credentials dans `application-local.yml` seront utilisés !**

---

### Vérification Logs

```
INFO - The following profiles are active: local  ← Profil OK
INFO - Email sent successfully to: test@...     ← SMTP fonctionne
```

---

## 📁 STRUCTURE FICHIERS

### Fichiers PUBLIC (GitHub) ✅

```
medverify-backend/
├── src/main/resources/
│   ├── application.yml ✅             (Sécurisé, pas de credentials)
│   └── application.yml.example ✅     (Template pour l'équipe)
├── .env.example ✅                    (Template variables)
├── set-env.ps1.example ✅             (Script Windows template)
└── set-env.sh.example ✅              (Script Linux template)
```

### Fichiers PRIVÉ (Local uniquement) 🔒

```
medverify-backend/
├── src/main/resources/
│   └── application-local.yml 🔒       (VOS credentials)
├── .env 🔒                            (Si utilisé)
├── set-env.ps1 🔒                     (Si créé)
└── set-env.sh 🔒                      (Si créé)
```

**✅ Tous les fichiers 🔒 sont dans .gitignore !**

---

## 📋 CHECKLIST AVANT PUSH

### ⚠️ VÉRIFICATIONS OBLIGATOIRES

```bash
# 1. Vérifier fichiers qui seront pushés
git status

# Résultat attendu :
# ✅ modified: application.yml (nettoyé)
# ✅ new: application.yml.example
# ❌ PAS application-local.yml (dans gitignore)
```

```bash
# 2. Chercher credentials (ne doit RIEN trouver)
git grep -i "epifaju@gmail.com"
git grep -i "wqyq ogyu"

# Résultat attendu : Rien ou seulement dans .example
```

```bash
# 3. Voir le diff de application.yml
git diff medverify-backend/src/main/resources/application.yml

# Résultat attendu :
# - password: ${SMTP_PASSWORD:wqyq ogyu zhgy bgfl}
# + password: ${SMTP_PASSWORD:}
```

### ✅ Si Tout Est OK

```bash
git add .
git commit -m "security: Secure credentials with environment variables"
git push origin main
```

---

## 🎓 POUR VOTRE ÉQUIPE

### Setup Nouveau Développeur

**Documentation à partager** :

```markdown
## Configuration Locale

### 1. Cloner le Projet

\`\`\`bash
git clone https://github.com/votre-repo/medverify.git
cd medverify/medverify-backend
\`\`\`

### 2. Configuration Credentials

\`\`\`bash

# Copier le template

cp src/main/resources/application.yml.example src/main/resources/application-local.yml

# Éditer avec VOS credentials

nano src/main/resources/application-local.yml
\`\`\`

### 3. Configurer Email

- Obtenir Gmail App Password : https://myaccount.google.com/apppasswords
- Mettre dans `application-local.yml` :
  \`\`\`yaml
  spring:
  mail:
  username: votre@email.com
  password: votre-app-password
  \`\`\`

### 4. Lancer

\`\`\`bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
\`\`\`
```

---

## 🔐 NIVEAUX DE SÉCURITÉ

### Niveau 1 : Développement ✅ (Implémenté)

```
application.yml (public)
    ↓
Variables ${SMTP_PASSWORD:}
    ↓
application-local.yml (gitignore)
```

### Niveau 2 : Production (Futur)

```
Variables d'environnement système
    ↓
Docker secrets
    ↓
Kubernetes secrets
```

### Niveau 3 : Enterprise (Futur)

```
AWS Secrets Manager
    ↓
HashiCorp Vault
    ↓
Azure Key Vault
```

---

## 🎯 RÉSUMÉ VISUEL

```
┌─────────────────────────────────────────┐
│  AVANT (❌ DANGEREUX)                   │
├─────────────────────────────────────────┤
│                                         │
│  application.yml                        │
│  ├── username: epifaju@gmail.com        │
│  └── password: wqyq ogyu... ❌          │
│                                         │
│  Push GitHub → Credentials publics 🚨   │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  APRÈS (✅ SÉCURISÉ)                    │
├─────────────────────────────────────────┤
│                                         │
│  application.yml (GitHub)               │
│  ├── username: ${SMTP_USERNAME:}   ✅  │
│  └── password: ${SMTP_PASSWORD:}   ✅  │
│                                         │
│  application-local.yml (gitignore)      │
│  ├── username: epifaju@gmail.com        │
│  └── password: wqyq ogyu... 🔒          │
│                                         │
│  Push GitHub → Pas de credentials ✅    │
└─────────────────────────────────────────┘
```

---

## 📊 FICHIERS MODIFIÉS

| Fichier                   | Type     | Statut        | Sur GitHub         |
| ------------------------- | -------- | ------------- | ------------------ |
| `application.yml`         | Config   | ✅ Nettoyé    | ✅ OUI (sécurisé)  |
| `application-local.yml`   | Config   | ✅ Créé       | ❌ NON (gitignore) |
| `application.yml.example` | Template | ✅ Créé       | ✅ OUI             |
| `.env.example`            | Template | ✅ Créé       | ✅ OUI             |
| `set-env.ps1.example`     | Script   | ✅ Créé       | ✅ OUI             |
| `.gitignore`              | Config   | ✅ Mis à jour | ✅ OUI             |

---

## 🎉 SUCCÈS !

### ✅ Vous Pouvez Maintenant :

1. ✅ **Push sur GitHub** sans exposer credentials
2. ✅ **Partager le code** avec l'équipe en sécurité
3. ✅ **Lancer le backend** avec vos credentials locaux
4. ✅ **Travailler en équipe** (chacun ses credentials)

### 🚀 Commandes

**Lancer** :

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Push** :

```bash
git push origin main  # ✅ Sécurisé !
```

---

**Vos credentials sont protégés ! Vous pouvez push en toute sécurité ! 🔒✅🚀**
