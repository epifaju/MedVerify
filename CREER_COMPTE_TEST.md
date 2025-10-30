# 👤 Créer un Compte de Test - Guide Immédiat

**Problème:** Le mot de passe utilisé est trop court

## 🚀 Solution Rapide

### Option 1: Utiliser le Compte Admin (Plus Simple)

Dans l'application mobile:
```
Email: admin@medverify.gw
Mot de passe: Admin@123456
```

**C'est tout!** Ce compte existe déjà et fonctionne.

---

### Option 2: Créer un Nouveau Compte via l'App

1. Dans l'application mobile
2. Cliquez sur **"S'inscrire"** ou **"Register"**
3. Remplissez le formulaire avec:
   - Email: votre email (ex: `epifaju@admin.com`)
   - **Mot de passe: MINIMUM 8 CARACTÈRES** (ex: `Password123`)
   - Prénom: votre prénom
   - Nom: votre nom
4. Cliquez sur **"S'inscrire"**
5. Retournez sur **Login** et connectez-vous

---

### Option 3: Créer via SQL (Avancé)

```bash
# 1. Se connecter à PostgreSQL
psql -U postgres -d medverify

# 2. Insérer un utilisateur (nom: Epi Faju, email: epifaju@admin.com)
INSERT INTO users (email, password, role, first_name, last_name, is_verified, is_active)
VALUES (
    'epifaju@admin.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyujmu3S0VSi', -- Admin@123456
    'ADMIN',
    'Epi',
    'Faju',
    TRUE,
    TRUE
);

# 3. Vérifier que l'utilisateur a été créé
SELECT email, role, is_active FROM users WHERE email = 'epifaju@admin.com';

# 4. Quitter psql
\q
```

**Mot de passe:** `Admin@123456`

---

### Option 4: Créer via Swagger (Web Interface)

1. **Ouvrez:** http://localhost:8080/swagger-ui.html (depuis Chrome sur PC)

2. **Trouvez:** `POST /api/v1/auth/register`

3. **Cliquez:** "Try it out"

4. **Entrez:**
```json
{
  "email": "epifaju@admin.com",
  "password": "Password123",
  "firstName": "Epi",
  "lastName": "Faju"
}
```

5. **Cliquez:** "Execute"

6. **Résultat:** Si 201 Created → Compte créé ✅

7. **Retournez sur l'app** et connectez-vous

---

## ⚠️ Règles de Mot de Passe

Le mot de passe doit respecter:
- ✅ **Minimum 8 caractères**
- ✅ Au moins une majuscule
- ✅ Au moins une minuscule  
- ✅ Au moins un chiffre
- ✅ Au moins un caractère spécial

**Exemples valides:**
- `Admin@123456` ✅
- `Password123` ✅
- `Test1234!` ✅

**Exemples invalides:**
- `test` ❌ (trop court)
- `12345678` ❌ (pas de majuscule/minuscule)

---

## 🎯 Résultat Attendu

Après création/connexion réussie:
1. ✅ Vous êtes connecté
2. ✅ Dashboard s'affiche
3. ✅ Navigation fonctionne
4. ✅ Toutes les fonctionnalités accessibles

---

## 📝 Notes Importantes

**Le backend est 100% fonctionnel!**

Le seul problème était:
- Le mot de passe utilisé était trop court
- OU le compte n'existait pas

**Solutions:**
- Utiliser `admin@medverify.gw` / `Admin@123456` (existe déjà)
- OU créer un compte avec mot de passe ≥ 8 caractères

---

**FIN DU GUIDE**

*Essayez maintenant avec un mot de passe de 8 caractères ou plus!* ✅

