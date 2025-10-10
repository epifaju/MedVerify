# 🧪 Guide Complet Swagger - MedVerify

## 🚀 Accéder à Swagger

**URL** : http://localhost:8080/swagger-ui.html

---

## 🔐 ÉTAPE 1 : Authentification

### 1.1 Login

1. Cherchez : **`POST /api/v1/auth/login`**
2. Cliquez sur **"Try it out"**
3. Entrez dans le champ `Request body` :

```json
{
  "email": "admin@medverify.gw",
  "password": "Admin@123456"
}
```

4. Cliquez sur **"Execute"**

### 1.2 Copier le Token

Dans la **réponse**, copiez le `accessToken` :

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBtZWR2ZXJpZnkuZ3ciLCJpYXQiOjE2OTczMjA...",
  "refreshToken": "a1b2c3d4...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": "...",
    "email": "admin@medverify.gw",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }
}
```

Copiez seulement la valeur du champ `accessToken` (le long texte après `"accessToken": "`).

### 1.3 Autoriser

1. **En haut à droite**, cliquez sur 🔒 **Authorize**
2. Dans le popup, entrez :

   ```
   Bearer eyJhbGciOiJIUzI1NiJ9...VOTRE_TOKEN_COMPLET
   ```

   ⚠️ **IMPORTANT** : Le mot **"Bearer "** (avec espace) est obligatoire !

3. Cliquez sur **"Authorize"**
4. Vous devriez voir : ✅ **"Authorized"**
5. Cliquez sur **"Close"**

---

## 🧪 ÉTAPE 2 : Tests des Fonctionnalités

### Test 1 : Vérifier un Médicament Authentique ✅

**Endpoint** : `POST /api/v1/medications/verify`

**Request body** :

```json
{
  "gtin": "03401234567890",
  "serialNumber": "SERIAL001",
  "batchNumber": "LOT2024A123",
  "expiryDate": "2025-12-31",
  "location": {
    "type": "Point",
    "coordinates": [-15.5989, 11.8632]
  }
}
```

**Résultat attendu** :

```json
{
  "status": "AUTHENTIC",
  "confidence": 1.0,
  "medication": {
    "name": "Paracétamol 500mg",
    "manufacturer": "Pharma Guinée",
    "gtin": "03401234567890"
  },
  "details": "Medication verified successfully",
  "alerts": []
}
```

---

### Test 2 : Détecter un Serial Dupliqué 🚨

Exécutez **6 fois** la même requête avec le même `serialNumber` :

```json
{
  "gtin": "03401234567890",
  "serialNumber": "DUPLICATE_TEST",
  "batchNumber": "LOT2024A123"
}
```

**Au 6ème appel** :

```json
{
  "status": "SUSPICIOUS",
  "confidence": 0.3,
  "alerts": [
    {
      "type": "SERIAL_DUPLICATE",
      "severity": "HIGH",
      "message": "Serial number scanned 6 times (threshold: 5)"
    }
  ]
}
```

---

### Test 3 : Détecter un Lot Rappelé 🚨

**Request** :

```json
{
  "gtin": "03401234567892",
  "serialNumber": "TEST123",
  "batchNumber": "LOT2023X999"
}
```

**Résultat** :

```json
{
  "status": "SUSPICIOUS",
  "confidence": 0.0,
  "alerts": [
    {
      "type": "BATCH_RECALLED",
      "severity": "HIGH",
      "message": "Batch LOT2023X999 has been recalled"
    }
  ]
}
```

---

### Test 4 : Créer un Signalement 📢

**Endpoint** : `POST /api/v1/reports`

**Request** :

```json
{
  "gtin": "03401234567890",
  "serialNumber": "FAKE123",
  "reportType": "COUNTERFEIT",
  "description": "Emballage suspect : couleur différente, fautes d'orthographe sur la boîte, goût bizarre du comprimé.",
  "purchaseLocation": {
    "type": "Point",
    "coordinates": [-15.5989, 11.8632]
  },
  "photos": [
    "https://example.com/photo1.jpg",
    "https://example.com/photo2.jpg"
  ],
  "anonymous": false
}
```

**Résultat** :

```json
{
  "reportId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUBMITTED",
  "referenceNumber": "REP-2025-ABC123",
  "message": "Report submitted successfully. Authorities have been notified.",
  "estimatedProcessingTime": "48-72 hours"
}
```

---

### Test 5 : Consulter Mes Signalements 📋

**Endpoint** : `GET /api/v1/reports/my-reports`

**Résultat** :

```json
[
  {
    "reportId": "...",
    "referenceNumber": "REP-2025-ABC123",
    "status": "SUBMITTED",
    "reportType": "COUNTERFEIT",
    "createdAt": "2025-10-09T21:10:00Z"
  }
]
```

---

### Test 6 : Dashboard Analytics (Admin) 📊

**Endpoint** : `GET /api/v1/admin/dashboard/stats?period=30d`

**Résultat** :

```json
{
  "period": {
    "start": "2025-09-09T00:00:00Z",
    "end": "2025-10-09T23:59:59Z",
    "days": 30
  },
  "kpis": {
    "totalScans": 15,
    "authenticityRate": 93.3,
    "totalReports": 2,
    "activeUsers": 3
  },
  "trends": {
    "scansGrowth": 25.0,
    "reportsGrowth": 50.0,
    "usersGrowth": 100.0
  },
  "topCounterfeits": [
    {
      "medication": {
        "gtin": "03401234567892",
        "name": "Azithromycine 250mg"
      },
      "count": 3,
      "percentage": 60.0
    }
  ],
  "geographicDistribution": [
    {
      "location": "Bissau",
      "scans": 8,
      "suspiciousScans": 2
    }
  ],
  "recentAlerts": [
    {
      "type": "SERIAL_DUPLICATE",
      "severity": "HIGH",
      "message": "Serial DUPLICATE_TEST scanned 6 times",
      "timestamp": "2025-10-09T21:00:00Z"
    }
  ]
}
```

---

### Test 7 : Rechercher un Médicament 🔍

**Endpoint** : `GET /api/v1/medications/search?name=paracetamol`

**Résultat** :

```json
[
  {
    "gtin": "03401234567890",
    "name": "Paracétamol 500mg",
    "manufacturer": "Pharma Guinée",
    "dosage": "500mg",
    "isEssential": true
  }
]
```

---

### Test 8 : Obtenir Détails d'un Médicament 💊

**Endpoint** : `GET /api/v1/medications/03401234567890`

**Résultat** :

```json
{
  "gtin": "03401234567890",
  "name": "Paracétamol 500mg",
  "manufacturer": "Pharma Guinée",
  "dosage": "500mg",
  "posology": {
    "adult": "1-2 comprimés toutes les 4-6h",
    "maxDailyDose": 4000,
    "unit": "mg"
  },
  "indications": ["Douleur", "Fièvre"],
  "sideEffects": ["Nausées rares", "Réactions allergiques"],
  "contraindications": ["Insuffisance hépatique sévère"],
  "isEssential": true,
  "requiresPrescription": false
}
```

---

## 📊 LISTE COMPLÈTE DES ENDPOINTS

### Authentication (Public)

- ✅ `POST /api/v1/auth/login` - Se connecter
- ✅ `POST /api/v1/auth/register` - S'inscrire
- ✅ `POST /api/v1/auth/refresh` - Rafraîchir le token

### Medications (Protégé)

- 🔒 `POST /api/v1/medications/verify` - Vérifier un médicament
- 🔒 `GET /api/v1/medications/{gtin}` - Détails médicament
- 🔒 `GET /api/v1/medications/search` - Rechercher
- 🔒 `GET /api/v1/medications/essential` - Liste médicaments essentiels

### Reports (Protégé)

- 🔒 `POST /api/v1/reports` - Créer un signalement
- 🔒 `GET /api/v1/reports/my-reports` - Mes signalements
- 🔒 `GET /api/v1/reports/{id}` - Détails signalement
- 🔒 `PUT /api/v1/admin/reports/{id}/review` - Réviser (Admin)

### Dashboard (Admin)

- 🔒 `GET /api/v1/admin/dashboard/stats` - Statistiques
- 🔒 `GET /api/v1/admin/reports` - Tous les signalements
- 🔒 `GET /api/v1/admin/reports?status=SUBMITTED` - Filtrer par statut

### Health

- ✅ `GET /actuator/health` - Santé du serveur

---

## 🎯 SCÉNARIOS DE TEST

### Scénario 1 : Patient Vérifie un Médicament

1. **Login** comme patient
2. **Verify** → GTIN valide → Status: AUTHENTIC
3. **Report** si suspect

### Scénario 2 : Détection de Contrefaçon

1. **Verify** avec serial dupliqué (6x)
2. Alert: SERIAL_DUPLICATE
3. **Create Report** avec photos
4. Admin **review** le signalement

### Scénario 3 : Autorité Consulte Dashboard

1. **Login** comme ADMIN
2. **GET** /admin/dashboard/stats
3. Voir KPIs, tendances, top counterfeits
4. **GET** /admin/reports pour voir tous les signalements

---

## 🔧 DÉPANNAGE

### Erreur 403 Forbidden

**Cause** : Pas authentifié ou token expiré

**Solution** :

1. Re-faites le login
2. Copiez le nouveau token
3. Re-cliquez sur Authorize

### Erreur 401 Unauthorized

**Cause** : Token invalide ou format incorrect

**Solution** :

- Vérifiez que vous avez bien mis **"Bearer "** (avec espace)
- Le token doit être : `Bearer eyJhbGci...`

### Token Expiré

**Cause** : Le token a une durée de 24h

**Solution** :

- Utilisez `POST /api/v1/auth/refresh` avec le refreshToken
- Ou re-faites un login

---

## 📚 DONNÉES DE TEST

### Médicaments Disponibles

| GTIN           | Nom                 | Batch Actif | Batch Rappelé |
| -------------- | ------------------- | ----------- | ------------- |
| 03401234567890 | Paracétamol 500mg   | LOT2024A123 | -             |
| 03401234567891 | Amoxicilline 500mg  | LOT2024B456 | -             |
| 03401234567892 | Azithromycine 250mg | LOT2024C789 | LOT2023X999   |

### Types de Reports

- `COUNTERFEIT` - Contrefaçon
- `QUALITY_ISSUE` - Problème de qualité
- `ADVERSE_REACTION` - Réaction indésirable
- `PACKAGING_DAMAGE` - Emballage endommagé
- `SUSPICIOUS_SOURCE` - Source suspecte
- `OTHER` - Autre

---

**Bon testing ! 🚀**
