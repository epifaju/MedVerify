# 🚀 LANCER BACKEND - Mode Sécurisé

## ✅ Configuration Sécurisée Activée

**Vos credentials sont protégés !**

---

## 🔧 COMMENT LANCER MAINTENANT

### Méthode 1 : Profil Local (Recommandé) ⭐

```bash
cd medverify-backend

# Lancer avec profil local
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**✅ Vos credentials dans `application-local.yml` seront utilisés automatiquement !**

---

### Méthode 2 : Variables d'Environnement

**Windows PowerShell** :

```powershell
$env:SMTP_USERNAME="epifaju@gmail.com"
$env:SMTP_PASSWORD="wqyq ogyu zhgy bgfl"
$env:JWT_SECRET="your-secret-key-256-bits"

./mvnw spring-boot:run
```

---

### Méthode 3 : Script Personnalisé

**Copier et éditer le template** :

```powershell
# 1. Copier
cp set-env.ps1.example set-env.ps1

# 2. Éditer set-env.ps1 avec vos credentials

# 3. Lancer
./set-env.ps1
```

---

## 🧪 VÉRIFICATION

### Logs Attendus au Démarrage

```
The following profiles are active: local  ← Profil local actif
...
Email sent successfully to: test@example.com  ← SMTP fonctionne
```

### Si Erreur Email

```
Email service not configured
```

**Solution** :

- Vérifier que le profil est actif : `-Dspring-boot.run.profiles=local`
- Vérifier `application-local.yml` existe
- Vérifier credentials dans `application-local.yml`

---

## 📝 RÉSUMÉ

**Commande unique** :

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

**Credentials** : Dans `application-local.yml` (sécurisé)

**GitHub** : Peut être pushé sans danger ✅

---

**C'est aussi simple que ça ! 🚀✅**
