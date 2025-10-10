# 🔐 Créer le Compte Admin - MedVerify

## 🚨 Problème

L'utilisateur `admin@medverify.gw` **n'existe pas encore** dans la base de données.

Les logs backend montrent :

```
Failed login attempt for user: admin@medverify.gw
Invalid credentials: Invalid email or password
```

---

## ✅ SOLUTION : Créer le Compte via l'Application

### Étape 1 : Aller sur l'Écran d'Inscription

**Dans l'application mobile** :

1. Sur l'écran de login, cherchez un bouton **"S'inscrire"** ou **"Register"**
2. OU créez un compte via Swagger

---

## 🎯 SOLUTION RAPIDE : Via Swagger

### Méthode 1 : Créer le Compte Admin

1. **Ouvrez Swagger** dans votre navigateur PC :

   ```
   http://localhost:8080/swagger-ui.html
   ```

2. **Allez sur** : `POST /api/v1/auth/register`

3. **Cliquez** sur "Try it out"

4. **Entrez ce JSON** :

   ```json
   {
     "email": "admin@medverify.gw",
     "password": "Admin@123456",
     "firstName": "Admin",
     "lastName": "User",
     "phone": "+245123456789",
     "role": "ADMIN"
   }
   ```

5. **Cliquez** sur "Execute"

6. **Résultat attendu** :
   ```json
   {
     "message": "User registered successfully"
   }
   ```

---

## 📱 SOLUTION MOBILE : Via l'Application

Si l'application a un écran Register, utilisez-le avec :

```
Email: admin@medverify.gw
Password: Admin@123456
First Name: Admin
Last Name: User
Phone: +245123456789
Role: ADMIN
```

---

## ✅ APRÈS CRÉATION

Une fois le compte créé, **retournez sur l'écran Login** et connectez-vous :

```
Email: admin@medverify.gw
Password: Admin@123456
```

**Résultat attendu** :

```
✅ Connexion réussie !
Bienvenue Admin !
```

---

## 🔧 ALTERNATIVE : Via SQL Direct (PostgreSQL)

Si vous préférez créer le compte directement en base :

1. **Ouvrez pgAdmin**
2. **Connectez-vous** à la base `medverify`
3. **Query Tool** → Exécutez ce script :

```sql
-- Créer l'utilisateur admin (le mot de passe sera Admin@123456)
-- Note: Ce hash BCrypt correspond au mot de passe "Admin@123456"
INSERT INTO users (
    email,
    password,
    first_name,
    last_name,
    role,
    phone,
    is_verified,
    is_active,
    failed_login_attempts,
    created_at,
    updated_at
) VALUES (
    'admin@medverify.gw',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye2JLmSaUjFTDvGnXp7h3Q5Jq6RKxVvmy',
    'Admin',
    'User',
    'ADMIN',
    '+245123456789',
    true,
    true,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;
```

4. **Redémarrez le backend** (optionnel)
5. **Testez la connexion** dans l'app

---

## 🎯 RECOMMANDATION

**Utilisez Swagger** (Méthode 1) car :

- ✅ Plus simple et rapide
- ✅ Utilise le service d'authentification
- ✅ Hash BCrypt automatique
- ✅ Pas besoin de SQL

---

## 📚 APRÈS CONNEXION

Une fois connecté, vous pourrez tester :

- ✅ Navigation dans l'app
- ✅ Déconnexion / Reconnexion
- ✅ Tous les endpoints API via Swagger
- ✅ Vérification de médicaments
- ✅ Création de signalements
- ✅ Dashboard analytics

---

**Créez le compte admin maintenant via Swagger !** 🚀
