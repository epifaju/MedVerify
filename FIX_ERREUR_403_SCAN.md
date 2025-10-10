# FIX: Erreur 403 Forbidden lors du Scan

**Date:** 9 octobre 2025  
**Statut:** ✅ EN COURS DE CORRECTION  
**Impact:** Critical - Empêche la vérification après scan

---

## 🐛 Problème

```
LOG  Code scanné (code128): 3400922385624
ERROR  Verify API error: 403
```

Le scanner **fonctionne** ✅ mais le backend retourne **403 Forbidden**.

---

## 🔍 Causes Possibles

### 1. **Token JWT Expiré** (15 minutes) - LE PLUS PROBABLE

- Access token expire après **15 minutes**
- Backend retourne 403 si token invalide
- **Solution:** Se reconnecter

### 2. **Rôle Utilisateur Incorrect**

- Endpoint `/api/v1/medications/verify` requiert:
  - `ROLE_PATIENT` ✅
  - `ROLE_PHARMACIST` ✅
  - `ROLE_AUTHORITY` ✅
  - `ROLE_ADMIN` ✅
- **Solution:** Vérifier le rôle dans la base

### 3. **CORS Bloqué**

- Le backend bloque les requêtes depuis l'IP mobile
- **Solution:** Vérifier `application.yml` CORS

---

## ✅ Solutions Appliquées

### 1. Amélioration Gestion Erreurs

J'ai ajouté des **messages spécifiques** pour chaque erreur:

```typescript
if (response.status === 403) {
  Alert.alert("🔒 Accès Refusé", "Votre session a expiré. Reconnectez-vous.", [
    { text: "OK", onPress: () => handleLogout() },
  ]);
}
```

### 2. Logging Détaillé

```typescript
console.log("Scanning GTIN:", gtin);
console.log(
  "Using token:",
  token ? `${token.substring(0, 20)}...` : "NO TOKEN"
);
console.log("Response status:", response.status);
```

---

## 🔧 Actions Immédiates

### Solution 1: Reconnectez-vous (LE PLUS SIMPLE)

1. **Appuyez sur 'r'** dans le terminal Expo pour recharger l'app
2. **Déconnectez-vous** (bouton en haut à droite)
3. **Reconnectez-vous:**
   - Email: `admin@medverify.gw`
   - Mot de passe: `Admin@123456`
4. **Réessayez le scan** ✅

### Solution 2: Vérifier le Backend

```bash
# Vérifier que le backend tourne
cd medverify-backend
mvn spring-boot:run

# Devrait afficher: "Started MedVerifyApplication"
```

### Solution 3: Tester avec Curl

```bash
# 1. Obtenir un nouveau token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@medverify.gw","password":"Admin@123456"}'

# Copier l'accessToken

# 2. Tester la vérification
curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer VOTRE_TOKEN" \
  -d '{"gtin":"3400922385624"}'
```

---

## 🔍 Vérifier la Configuration CORS

Le backend doit autoriser les requêtes depuis votre mobile:

```yaml
# medverify-backend/src/main/resources/application.yml
cors:
  allowed-origins: "*" # ou "http://192.168.1.16:8081"
  allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
  allowed-headers: "*"
  allow-credentials: true
```

---

## 🎯 Message Amélioré

### Avant:

```
ERROR Verify API error: 403
```

### Après:

```
🔒 Accès Refusé
Votre session a expiré ou vous n'avez pas les permissions. Reconnectez-vous.
[OK] ← Déconnecte automatiquement
```

---

## 📝 Logs Console Améliorés

Vous verrez maintenant dans la console:

```javascript
LOG  Scanning GTIN: 3400922385624
LOG  Using token: eyJhbGciOiJIUzI1NiIs...
LOG  Response status: 403
ERROR Verify API error: 403 {"message":"Access Denied"}
```

---

## ✅ Checklist Debug

- [ ] Backend démarré ? (`mvn spring-boot:run`)
- [ ] URL correcte ? (192.168.1.16:8080)
- [ ] Session expirée ? (> 15 minutes)
- [ ] Token valide ? (vérifier console logs)
- [ ] CORS configuré ? (application.yml)

---

## 🎉 Solution Immédiate

**Rechargez l'app (appuyez sur 'r') et reconnectez-vous !**

Le message vous indiquera maintenant clairement si c'est un problème de session expirée (403/401).

---

**Prochaine amélioration:** Implémenter le refresh automatique du token (401 → refresh → retry)

