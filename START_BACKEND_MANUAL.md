# 🚀 Démarrage manuel du Backend

## Le problème

Le backend a été configuré avec le CORS correctement, mais il faut le démarrer manuellement pour voir les logs.

---

## ✅ Solution : Démarrage en 3 étapes

### Étape 1 : Ouvrir un NOUVEAU terminal PowerShell

1. **Cliquer sur l'icône PowerShell** dans la barre des tâches
2. **OU** appuyer sur `Windows + X` → choisir "Windows PowerShell"

---

### Étape 2 : Naviguer vers le dossier backend

Dans le nouveau terminal, tapez :

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\medverify-backend
```

---

### Étape 3 : Démarrer le backend

```powershell
mvn spring-boot:run
```

**Attendez environ 30-60 secondes**

Vous devriez voir à la fin :

```
Started MedVerifyApplication in X.XXX seconds
```

---

## 🎯 Une fois le backend démarré

### Test 1 : Swagger

Dans votre navigateur PC, allez à :

```
http://192.168.1.16:8080/swagger-ui.html
```

Vous devriez voir la documentation de l'API.

---

### Test 2 : Depuis votre téléphone

Dans le navigateur de votre téléphone, allez à :

```
http://192.168.1.16:8080/swagger-ui.html
```

Si ça charge, le backend est accessible depuis le téléphone !

---

### Test 3 : Se connecter dans l'app

1. Ouvrir **Expo Go** sur le téléphone
2. **Scanner le QR code** (ou recharger si déjà ouvert)
3. **Entrer email et mot de passe**
4. **Cliquer sur "Se connecter"**

✅ **Ça devrait fonctionner maintenant !**

---

## 🔍 Vérifier les logs en temps réel

Dans le terminal où le backend tourne, vous verrez les requêtes en temps réel :

```
POST /api/v1/auth/login
GET /api/v1/medications/scan/...
```

---

## 📝 Credentials par défaut

Si vous n'avez pas encore de compte, créez-en un :

### Option 1 : Via Swagger

1. Aller à `http://192.168.1.16:8080/swagger-ui.html`
2. Section **Auth Controller**
3. `POST /api/v1/auth/register`
4. Cliquer sur "Try it out"
5. Remplir les champs :

```json
{
  "email": "test@medverify.gw",
  "password": "Test123!",
  "firstName": "Test",
  "lastName": "User",
  "language": "fr"
}
```

---

### Option 2 : Via SQL (compte admin)

```sql
-- Dans PostgreSQL
INSERT INTO users (email, password, first_name, last_name, role, language, is_active, created_at, updated_at)
VALUES (
  'admin@medverify.gw',
  '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQNkhFeMEyPQ9Q/cKbYJ.', -- mot de passe : admin123
  'Admin',
  'MedVerify',
  'AUTHORITY',
  'fr',
  true,
  NOW(),
  NOW()
);
```

**Credentials** :

- Email : `admin@medverify.gw`
- Mot de passe : `admin123`

---

## 🛑 Arrêter le backend

Dans le terminal où le backend tourne :

1. Appuyer sur `Ctrl+C`
2. Le backend s'arrête proprement

---

## ✅ Checklist

- [ ] Nouveau terminal PowerShell ouvert
- [ ] Navigué vers `medverify-backend`
- [ ] Lancé `mvn spring-boot:run`
- [ ] Attendu que "Started MedVerifyApplication" s'affiche
- [ ] Vérifié Swagger : `http://192.168.1.16:8080/swagger-ui.html`
- [ ] Testé la connexion dans l'app mobile

---

## 🚨 Si ça ne démarre pas

### Erreur : Port 8080 déjà utilisé

```powershell
# Trouver le processus
Get-NetTCPConnection -LocalPort 8080 | Select-Object -ExpandProperty OwningProcess | ForEach-Object { Stop-Process -Id $_ -Force }

# Puis relancer
mvn spring-boot:run
```

---

### Erreur : PostgreSQL non accessible

Vérifier que PostgreSQL tourne :

```powershell
Get-Service postgresql*
```

Si arrêté :

```powershell
Start-Service postgresql-x64-13
```

---

**Allez-y, lancez le backend manuellement dans un nouveau terminal ! 🚀**
