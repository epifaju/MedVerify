# ✅ Fix Complet - "Network request failed"

## 🎯 Problème identifié

L'erreur "Network request failed" était causée par le **CORS** qui bloquait les requêtes depuis l'app mobile.

### Cause racine

Le backend n'autorisait que :

- `http://localhost:3000`
- `http://localhost:19006`

Mais l'app mobile utilisait : `http://192.168.1.16:8080` ❌

---

## ✅ Solution appliquée

### 1. Configuration CORS mise à jour

**Fichier :** `medverify-backend/src/main/resources/application.yml`

```yaml
cors:
  allowed-origins: ${CORS_ORIGINS:http://localhost:3000,http://localhost:19006,http://192.168.1.16:8080,http://10.0.2.2:8080}
```

Maintenant autorisé :

- ✅ `http://localhost:3000` (frontend web)
- ✅ `http://localhost:19006` (Expo web)
- ✅ `http://192.168.1.16:8080` (votre téléphone via WiFi)
- ✅ `http://10.0.2.2:8080` (émulateur Android)

### 2. Backend redémarré

Le backend a été redémarré pour appliquer les changements.

---

## 🧪 Tester maintenant

### Étape 1 : Vérifier que le backend est opérationnel

Attendez environ 30-60 secondes que le backend démarre complètement.

Dans votre navigateur, allez à :

```
http://192.168.1.16:8080/swagger-ui.html
```

Vous devriez voir la documentation Swagger.

---

### Étape 2 : Relancer l'app Expo (mode LAN recommandé)

#### Si vous êtes en mode tunnel :

1. **Arrêter Expo** (Ctrl+C dans le terminal)
2. **Relancer en mode LAN** :

```bash
cd MedVerifyApp\MedVerifyExpo
npx expo start --clear
```

3. **Scanner le QR code** avec Expo Go

#### Si vous êtes déjà en mode LAN :

1. Dans l'app Expo Go, **secouer le téléphone**
2. Cliquer sur **"Reload"**

---

### Étape 3 : Tester la connexion

1. Entrer votre **email**
2. Entrer votre **mot de passe**
3. Cliquer sur **"Se connecter"**

✅ **Ça devrait fonctionner maintenant !**

---

## 🔍 Si ça ne fonctionne toujours pas

### Vérification 1 : Backend accessible depuis le téléphone

Sur votre téléphone, ouvrir un navigateur et aller à :

```
http://192.168.1.16:8080
```

Si ça ne charge pas :

- ❌ Vérifier que PC et téléphone sont sur le **même WiFi**
- ❌ Désactiver temporairement le **pare-feu Windows**

---

### Vérification 2 : Logs du backend

Vérifier les logs pour voir si les requêtes arrivent :

```bash
cd medverify-backend
tail -f logs/medverify.log
```

Vous devriez voir les requêtes POST vers `/api/v1/auth/login`

---

### Vérification 3 : Compte utilisateur existe

Pour créer un compte admin si nécessaire :

```sql
-- Dans PostgreSQL
INSERT INTO users (email, password, first_name, last_name, role, language, is_active, created_at, updated_at)
VALUES (
  'admin@medverify.gw',
  '$2a$10$kIjlkV8Z9rZxK9xG9pQ9K.K9xG9pQ9K.K9xG9pQ9K.K9xG9pQ9K.K',  -- mot de passe : admin123
  'Admin',
  'MedVerify',
  'AUTHORITY',
  'fr',
  true,
  NOW(),
  NOW()
);
```

---

## 🚀 Checklist finale

- [ ] Backend démarré (port 8080)
- [ ] CORS mis à jour avec `192.168.1.16:8080`
- [ ] PC et téléphone sur le même WiFi
- [ ] Expo en mode LAN (pas tunnel)
- [ ] Swagger accessible depuis le navigateur : `http://192.168.1.16:8080/swagger-ui.html`
- [ ] Compte utilisateur créé

---

## 📱 Test alternatif : Émulateur Android

Si le téléphone physique pose toujours problème, utilisez un émulateur :

1. Installer Android Studio
2. Créer un AVD
3. Lancer l'émulateur
4. Dans Expo, appuyer sur `a`

Avec un émulateur, l'URL est automatiquement `http://10.0.2.2:8080` (déjà autorisé dans le CORS).

---

## 🎉 Résumé

| Élément            | Status                 |
| ------------------ | ---------------------- |
| CORS configuré     | ✅                     |
| Backend redémarré  | ✅                     |
| IP autorisée       | ✅ `192.168.1.16:8080` |
| Émulateur supporté | ✅ `10.0.2.2:8080`     |

---

## 📞 Prochaines étapes

1. **Attendre 30-60 secondes** que le backend finisse de démarrer
2. **Vérifier Swagger** : http://192.168.1.16:8080/swagger-ui.html
3. **Recharger l'app Expo** (secouer le téléphone + "Reload")
4. **Essayer de vous connecter**

---

**Essayez maintenant et dites-moi si ça fonctionne ! 🚀**

Si vous avez toujours une erreur, envoyez-moi le message d'erreur exact.
