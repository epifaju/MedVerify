# 🚀 Guide Rapide - Vérification Email

## ✅ Fonctionnalité Implémentée

**Vérification email avec code à 6 chiffres** pour activation de compte.

---

## 🔧 Configuration Requise

### 1. Configuration SMTP (Backend)

**Éditer** : `medverify-backend/src/main/resources/application.yml`

**Ajouter** :

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: votre-email@gmail.com
    password: votre-app-password # App Password Gmail
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

app:
  verification:
    code-expiry-minutes: 15 # Durée validité code
    max-pending-codes: 5 # Limite anti-spam
```

### 2. Gmail App Password

1. Aller sur https://myaccount.google.com/apppasswords
2. Générer un App Password
3. Copier dans `spring.mail.password`

---

## 🧪 Test Rapide

### Étape 1 : Redémarrer Backend

```bash
cd medverify-backend
./mvnw spring-boot:run
```

**Vérifier logs** :

```
Successfully validated 9 migrations  ← Migration V9 appliquée
Current version of schema "public": 9
```

### Étape 2 : S'inscrire dans l'App

1. Lancer l'app mobile
2. Cliquer "S'inscrire"
3. Remplir le formulaire
4. Cliquer "S'inscrire"
5. ✅ **Redirection automatique vers VerifyEmailScreen**

### Étape 3 : Récupérer le Code

**Option A - Logs Backend** :

```
INFO  - Verification code created: [UUID] (expires in 15 minutes)
```

Le code est dans les logs si l'email n'est pas configuré.

**Option B - Email** :
Si SMTP configuré, consultez votre boîte email.

### Étape 4 : Vérifier

1. Entrer le code à 6 chiffres
2. Cliquer "Vérifier"
3. ✅ Message de succès
4. ✅ Redirection vers Login
5. ✅ Se connecter normalement

---

## 📋 Checklist Fonctionnalités

- [x] ✅ Génération code 6 chiffres
- [x] ✅ Envoi email automatique
- [x] ✅ Expiration 15 minutes
- [x] ✅ Limite 3 tentatives
- [x] ✅ Renvoi de code
- [x] ✅ Écran frontend complet
- [x] ✅ Multilingue (FR/PT/CR)
- [x] ✅ Sécurité (anti-spam, validation)
- [x] ✅ API REST documentée

---

## 🎯 API Endpoints Disponibles

| Endpoint                   | Méthode | Description                |
| -------------------------- | ------- | -------------------------- |
| `/api/v1/auth/register`    | POST    | Inscription + envoi code   |
| `/api/v1/auth/verify`      | POST    | Vérifier code              |
| `/api/v1/auth/resend-code` | POST    | Renvoyer code              |
| `/api/v1/auth/login`       | POST    | Connexion (compte vérifié) |

---

## 💡 Notes

### Code en Logs (Development)

Si email non configuré, le code apparaît dans les logs :

```bash
# Chercher dans les logs
grep "Verification code created" logs/application.log
```

### Production

En production, **obligatoirement** configurer un vrai service SMTP (Gmail, SendGrid, AWS SES).

---

**La vérification email est opérationnelle ! 📧✅**



