# 📧 Configuration Email - Vérification Code 6 Chiffres

## ⚙️ Configuration SMTP Requise

Pour que la vérification email fonctionne, vous devez configurer un serveur SMTP.

---

## 🔧 Option 1 : Gmail (Recommandé pour Tests)

### Étape 1 : Générer App Password

1. Aller sur https://myaccount.google.com/apppasswords
2. Sélectionner "Application" → "Autre (nom personnalisé)"
3. Taper "MedVerify"
4. Cliquer "Générer"
5. **Copier** le mot de passe de 16 caractères

### Étape 2 : Configuration

**Fichier** : `medverify-backend/src/main/resources/application.yml`

**Ajouter** (ou modifier) :

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: votre-email@gmail.com
    password: xxxx xxxx xxxx xxxx # App Password (16 caractères)
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000

# Configuration vérification
app:
  verification:
    code-expiry-minutes: 15 # Code valide 15 minutes
    max-pending-codes: 5 # Max 5 codes non utilisés par user
```

### Étape 3 : Redémarrer Backend

```bash
cd medverify-backend
./mvnw spring-boot:run
```

### Étape 4 : Vérifier Logs

```
Email sent successfully to: test@example.com
```

---

## 🔧 Option 2 : Mailtrap (Tests sans vrai email)

**Site** : https://mailtrap.io (Gratuit)

### Configuration

```yaml
spring:
  mail:
    host: sandbox.smtp.mailtrap.io
    port: 2525
    username: votre-username-mailtrap
    password: votre-password-mailtrap
    properties:
      mail:
        smtp:
          auth: true
```

**Avantages** :

- ✅ Pas besoin de vrai serveur email
- ✅ Interface web pour voir les emails
- ✅ Parfait pour développement

---

## 🔧 Option 3 : SendGrid (Production)

**Pour production** : https://sendgrid.com

```yaml
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: votre-api-key-sendgrid
```

**Avantages** :

- ✅ Fiable
- ✅ Analytics
- ✅ 100 emails/jour gratuit

---

## 🧪 Test Sans Configuration Email

### Mode Développement

Si vous **ne configurez pas** SMTP, le code apparaîtra **dans les logs backend** :

```bash
# Chercher le code dans les logs
grep "Verification code created" logs/application.log
```

**Log exemple** :

```
INFO  - Verification code created: 8f3a2... (expires in 15 minutes)
INFO  - Email service not configured. Skipping email to: test@example.com
```

Le code est dans la base de données, récupérable via :

```sql
SELECT code, email, expires_at
FROM verification_codes
WHERE email = 'test@example.com'
ORDER BY created_at DESC
LIMIT 1;
```

---

## 📊 Variables d'Environnement (Optionnel)

Au lieu de `application.yml`, vous pouvez utiliser des variables d'environnement :

```bash
# Windows PowerShell
$env:SPRING_MAIL_HOST="smtp.gmail.com"
$env:SPRING_MAIL_PORT="587"
$env:SPRING_MAIL_USERNAME="your-email@gmail.com"
$env:SPRING_MAIL_PASSWORD="your-app-password"

# Linux/Mac
export SPRING_MAIL_HOST=smtp.gmail.com
export SPRING_MAIL_PORT=587
export SPRING_MAIL_USERNAME=your-email@gmail.com
export SPRING_MAIL_PASSWORD=your-app-password

# Puis lancer
./mvnw spring-boot:run
```

---

## ✅ Vérification Configuration

### Test Email Simple

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!",
    "firstName": "Test",
    "lastName": "User",
    "phone": "+245912345678",
    "role": "PATIENT"
  }'
```

**Résultat attendu** :

```json
{
  "message": "Registration successful. Please check your email for the verification code."
}
```

**Vérifier logs** :

```
INFO  - User registered successfully: test@example.com
INFO  - Verification code created: ...
INFO  - Email sent successfully to: test@example.com  ← Si SMTP OK
```

---

## 🐛 Troubleshooting

### Erreur : "Failed to send email"

**Cause** : SMTP mal configuré

**Solutions** :

1. Vérifier username/password
2. Vérifier port (587 pour TLS, 465 pour SSL)
3. Vérifier App Password Gmail (pas le mot de passe normal)
4. Vérifier pare-feu (autoriser port 587)

### Erreur : "Authentication failed"

**Cause** : Mauvais credentials

**Solution** :

- Gmail : Utiliser App Password, pas le mot de passe normal
- Activer 2FA sur Gmail d'abord

### Pas d'erreur mais pas d'email

**Cause** : Email envoyé en spam

**Solution** :

- Vérifier dossier spam
- Ajouter expediteur aux contacts

---

## 📝 Checklist Configuration

- [ ] SMTP configuré dans `application.yml`
- [ ] Backend redémarré
- [ ] Test inscription fonctionnel
- [ ] Email reçu dans boîte de réception
- [ ] Code vérifiable dans l'app
- [ ] Activation compte réussie

---

**Une fois configuré, la vérification email fonctionnera automatiquement ! 📧✅**



