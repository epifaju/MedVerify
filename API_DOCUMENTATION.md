# 📚 Documentation API MedVerify

**Version** : 1.0.0  
**Base URL** : `http://localhost:8080/api/v1` (dev) | `https://api.medverify.gw/api/v1` (prod)  
**Format** : JSON

---

## 📋 Table des Matières

1. [Overview](#overview)
2. [Authentification](#authentification)
3. [Endpoints](#endpoints)
4. [Codes d'erreur](#codes-derreur)
5. [Rate Limiting](#rate-limiting)
6. [Exemples](#exemples)

---

## 🔐 Authentification

L'API utilise **JWT (JSON Web Tokens)** pour l'authentification.

### Flux d'authentification

1. **Inscription** : `POST /api/v1/auth/register`
   - Créer un nouveau compte
   - Email de vérification envoyé automatiquement

2. **Vérification email** : `POST /api/v1/auth/verify`
   - Activer le compte avec le code reçu par email

3. **Connexion** : `POST /api/v1/auth/login`
   - Obtenir `accessToken` et `refreshToken`

4. **Utilisation** : Inclure le token dans le header
   ```
   Authorization: Bearer {accessToken}
   ```

5. **Refresh** : `POST /api/v1/auth/refresh` (quand accessToken expire)
   - Utiliser le `refreshToken` pour obtenir un nouveau `accessToken`

### Tokens

- **Access Token** : Durée de vie courte (~15 minutes)
- **Refresh Token** : Durée de vie longue (~7 jours)

---

## 🔌 Endpoints

### Authentification

#### POST `/api/v1/auth/register`

Créer un nouveau compte utilisateur.

**Body** :
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+245912345678",
  "role": "PATIENT"
}
```

**Réponses** :
- `201 Created` - Compte créé, email de vérification envoyé
- `400 Bad Request` - Données invalides
- `409 Conflict` - Email déjà utilisé

---

#### POST `/api/v1/auth/login`

Authentifier un utilisateur.

**Body** :
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Réponse** (`200 OK`) :
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Erreurs** :
- `401 Unauthorized` - Credentials invalides
- `423 Locked` - Compte verrouillé (trop de tentatives échouées)

---

#### POST `/api/v1/auth/refresh`

Rafraîchir l'access token.

**Body** :
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Réponse** (`200 OK`) : Même format que `/login`

---

#### POST `/api/v1/auth/verify`

Vérifier l'email avec le code à 6 chiffres.

**Body** :
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

---

### Médicaments

#### POST `/api/v1/medications/verify` 🔒

Vérifier l'authenticité d'un médicament.

**Headers** :
```
Authorization: Bearer {accessToken}
```

**Body** :
```json
{
  "gtin": "03401234567890",
  "serialNumber": "SN123456",
  "batchNumber": "LOT2024A123",
  "expiryDate": "2026-12-31",
  "location": {
    "latitude": 11.8636,
    "longitude": -15.5984
  }
}
```

**Réponse** (`200 OK`) :
```json
{
  "verificationId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "AUTHENTIC",
  "confidence": 0.95,
  "verificationSource": "CACHE_LOCAL",
  "verifiedAt": "2025-11-01T12:00:00Z",
  "medication": {
    "id": "...",
    "gtin": "03401234567890",
    "name": "Paracétamol 500mg",
    "genericName": "Paracétamol",
    "manufacturer": "PharmaCare Labs"
  },
  "alerts": []
}
```

**Statuts possibles** :
- `AUTHENTIC` - Médicament authentique (confidence ≥ 0.7)
- `SUSPICIOUS` - Médicament suspect (confidence < 0.7)
- `UNKNOWN` - Médicament non trouvé

**Erreurs** :
- `400 Bad Request` - GTIN invalide
- `401 Unauthorized` - Token manquant ou invalide
- `429 Too Many Requests` - Rate limit dépassé

---

#### GET `/api/v1/medications/{id}` 🔒

Récupérer les détails d'un médicament par ID.

**Headers** :
```
Authorization: Bearer {accessToken}
```

**Réponse** (`200 OK`) :
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "gtin": "03401234567890",
  "name": "Paracétamol 500mg",
  "genericName": "Paracétamol",
  "manufacturer": "PharmaCare Labs",
  "dosage": "500mg",
  "pharmaceuticalForm": "Comprimé",
  "isEssential": true,
  "isActive": true
}
```

---

#### GET `/api/v1/medications/search?name={name}` 🔒

Rechercher des médicaments par nom.

**Query Parameters** :
- `name` (required) - Nom ou partie du nom à rechercher

**Exemple** :
```
GET /api/v1/medications/search?name=paracétamol
```

---

#### GET `/api/v1/medications/essential` 🔒

Récupérer la liste des médicaments essentiels.

**Réponse** (`200 OK`) : Array de `Medication`

---

### Pharmacies

#### GET `/api/v1/pharmacies` 🌐

Liste des pharmacies (endpoint public, pas d'authentification requise).

**Query Parameters** :
- `city` (optional) - Filtrer par ville
- `openNow` (optional) - Seulement les pharmacies ouvertes maintenant
- `onDuty` (optional) - Seulement les pharmacies de garde
- `latitude` (optional) - Latitude pour tri par distance
- `longitude` (optional) - Longitude pour tri par distance

---

## ❌ Codes d'erreur

| Code | Description | Exemples |
|------|-------------|----------|
| `400` | Bad Request | Données invalides, format incorrect |
| `401` | Unauthorized | Token manquant, expiré ou invalide |
| `403` | Forbidden | Pas de permissions (rôle insuffisant) |
| `404` | Not Found | Ressource introuvable |
| `409` | Conflict | Ressource déjà existante (email dupliqué) |
| `423` | Locked | Compte verrouillé (trop de tentatives) |
| `429` | Too Many Requests | Rate limit dépassé |
| `500` | Internal Server Error | Erreur serveur |

---

## 🚦 Rate Limiting

Certains endpoints sont protégés par un rate limiting pour prévenir les abus :

| Endpoint | Limite | Fenêtre |
|----------|--------|---------|
| `/api/v1/auth/login` | 5 requêtes | 15 minutes |
| `/api/v1/auth/register` | 3 requêtes | 1 heure |
| `/api/v1/auth/verify-email` | 10 requêtes | 1 heure |
| `/api/v1/medications/verify` | 30 requêtes | 1 minute |
| Autres endpoints | 100 requêtes | 1 minute |

**Headers de réponse** :
- `X-RateLimit-Limit` - Nombre maximum de requêtes
- `X-RateLimit-Remaining` - Nombre de requêtes restantes
- `X-RateLimit-Reset` - Timestamp de réinitialisation

**En cas de dépassement** : `429 Too Many Requests`

---

## 📝 Exemples

### Exemple complet : Vérifier un médicament

```bash
# 1. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!"
  }'

# Réponse :
# {
#   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "refreshToken": "..."
# }

# 2. Vérifier un médicament
curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d '{
    "gtin": "03401234567890",
    "serialNumber": "SN123456",
    "batchNumber": "LOT2024A123"
  }'
```

### Exemple : Gérer le refresh token

```bash
# Quand l'access token expire (401)
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'

# Obtenir un nouveau access token
```

---

## 🔧 Swagger UI

La documentation interactive est disponible via Swagger UI :

**URL** : `http://localhost:8080/swagger-ui.html` (en développement)

**Fonctionnalités** :
- Tester les endpoints directement depuis le navigateur
- Authentification JWT intégrée (bouton "Authorize")
- Exemples de requêtes/réponses
- Schémas complets des DTOs

---

## 📞 Support

Pour toute question ou problème :
- **Email** : support@medverify.gw
- **Documentation** : https://docs.medverify.gw

---

**Dernière mise à jour** : 2025-11-01

