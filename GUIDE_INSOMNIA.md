# 🚀 Guide Insomnia - Tester MedVerify API

## 🎯 CONFIGURATION RAPIDE (5 Minutes)

### Étape 1 : Créer une Collection

1. Ouvrez **Insomnia**
2. Cliquez sur **"Create"** → **"Request Collection"**
3. Nom : **"MedVerify API"**

---

## 🔐 ÉTAPE 2 : Authentification

### 1. Créer la Requête Login

1. **New Request** (dans votre collection)
2. **Nom** : `Login Admin`
3. **Method** : **POST**
4. **URL** : `http://localhost:8080/api/v1/auth/login`
5. **Body** → **JSON** :
   ```json
   {
     "email": "admin@medverify.gw",
     "password": "Admin@123456"
   }
   ```
6. **Send** ✅

### 2. Résultat Attendu

Vous devriez voir :

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBtZWR2ZXJpZnkuZ3ciLCJpYXQi...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": "...",
    "email": "admin@medverify.gw",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN",
    "isVerified": true
  }
}
```

### 3. Copier le Token

**Sélectionnez et copiez** seulement la valeur du champ `accessToken` (le long texte).

---

## 🔧 ÉTAPE 3 : Configurer l'Authentification Globale

### Méthode 1 : Environment Variable (Recommandé)

1. Cliquez sur **"No Environment"** (en haut à gauche)
2. **"Manage Environments"**
3. **"+"** pour créer un nouvel environnement
4. Nom : `MedVerify Local`
5. Ajoutez :
   ```json
   {
     "base_url": "http://localhost:8080/api/v1",
     "token": "COLLEZ_VOTRE_TOKEN_ICI"
   }
   ```
6. **Done**
7. Sélectionnez l'environnement **"MedVerify Local"**

### Méthode 2 : Bearer Token Direct

Pour chaque requête protégée :

1. Onglet **"Auth"**
2. Type : **"Bearer Token"**
3. Token : Collez votre `accessToken`

---

## 🧪 ÉTAPE 4 : Tests des Endpoints

### Test 1 : Vérifier un Médicament ✅

1. **New Request**
2. Nom : `Verify Medication`
3. Method : **POST**
4. URL : `http://localhost:8080/api/v1/medications/verify`
5. **Auth** → **Bearer Token** → Collez votre token
6. **Body** → **JSON** :
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
7. **Send**

**Résultat** :

```json
{
  "verificationId": "...",
  "status": "AUTHENTIC",
  "confidence": 1.0,
  "medication": {
    "gtin": "03401234567890",
    "name": "Paracétamol 500mg",
    "manufacturer": "Pharma Guinée",
    "dosage": "500mg"
  },
  "details": "Medication verified successfully",
  "alerts": [],
  "verifiedAt": "2025-10-09T21:15:00Z"
}
```

---

### Test 2 : Détecter Serial Dupliqué 🚨

1. **Dupliquez** la requête précédente
2. Nom : `Verify Duplicate Serial`
3. Changez le `serialNumber` : `"DUPLICATE_TEST"`
4. **Exécutez 6 fois** (Send, Send, Send...)

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

### Test 3 : Créer un Signalement 📢

1. **New Request**
2. Nom : `Create Report`
3. Method : **POST**
4. URL : `http://localhost:8080/api/v1/reports`
5. **Auth** → **Bearer Token** → Token
6. **Body** → **JSON** :
   ```json
   {
     "gtin": "03401234567890",
     "serialNumber": "FAKE123",
     "reportType": "COUNTERFEIT",
     "description": "Emballage suspect : couleur différente, fautes d'orthographe, goût bizarre.",
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
7. **Send**

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

### Test 4 : Dashboard Analytics 📊

1. **New Request**
2. Nom : `Dashboard Stats`
3. Method : **GET**
4. URL : `http://localhost:8080/api/v1/admin/dashboard/stats?period=30d`
5. **Auth** → **Bearer Token** → Token
6. **Send**

**Résultat** :

```json
{
  "period": {
    "start": "2025-09-09T00:00:00Z",
    "end": "2025-10-09T23:59:59Z",
    "days": 30
  },
  "kpis": {
    "totalScans": 7,
    "authenticityRate": 85.7,
    "totalReports": 1,
    "activeUsers": 1
  },
  "trends": {
    "scansGrowth": 0.0,
    "reportsGrowth": 0.0,
    "usersGrowth": 0.0
  },
  "topCounterfeits": [],
  "geographicDistribution": [
    {
      "location": "Unknown",
      "scans": 7,
      "suspiciousScans": 1
    }
  ],
  "recentAlerts": [
    {
      "type": "SERIAL_DUPLICATE",
      "severity": "HIGH",
      "message": "Serial number DUPLICATE_TEST scanned 6 times",
      "timestamp": "2025-10-09T21:15:00Z"
    }
  ]
}
```

---

### Test 5 : Mes Signalements 📋

1. **New Request**
2. Nom : `My Reports`
3. Method : **GET**
4. URL : `http://localhost:8080/api/v1/reports/my-reports`
5. **Auth** → **Bearer Token** → Token
6. **Send**

---

### Test 6 : Rechercher un Médicament 🔍

1. **New Request**
2. Nom : `Search Medication`
3. Method : **GET**
4. URL : `http://localhost:8080/api/v1/medications/search?name=paracetamol`
5. **Auth** → **Bearer Token** → Token
6. **Send**

---

### Test 7 : Détails d'un Médicament 💊

1. **New Request**
2. Nom : `Get Medication Details`
3. Method : **GET**
4. URL : `http://localhost:8080/api/v1/medications/03401234567890`
5. **Auth** → **Bearer Token** → Token
6. **Send**

---

## 📦 IMPORTER UNE COLLECTION COMPLÈTE

Je peux créer un fichier JSON Insomnia avec **toutes les requêtes** pré-configurées !

### Fichier : `insomnia_collection.json`

Créez ce fichier et importez-le dans Insomnia :

**Menu** → **Import/Export** → **Import Data** → **From File**

---

## 🎯 WORKFLOW RECOMMANDÉ

### Organisation des Requêtes

1. **Dossier "Auth"**

   - Login
   - Register
   - Refresh Token

2. **Dossier "Medications"**

   - Verify
   - Get Details
   - Search
   - List Essential

3. **Dossier "Reports"**

   - Create Report
   - My Reports
   - Get Report Details

4. **Dossier "Admin"**
   - Dashboard Stats
   - All Reports
   - Review Report

---

## 💡 ASTUCES INSOMNIA

### Raccourcis Utiles

- **`Ctrl + Enter`** : Envoyer la requête
- **`Ctrl + K`** : Rechercher une requête
- **`Ctrl + N`** : Nouvelle requête

### Utiliser les Variables

Vous pouvez utiliser `{{ _.token }}` dans les headers après avoir configuré l'environnement.

### Historique

Insomnia garde l'historique de toutes vos requêtes !

---

## ✅ AVANTAGES D'INSOMNIA

Par rapport à Swagger :

- ✅ Plus visuel et intuitif
- ✅ Sauvegarde automatique des requêtes
- ✅ Gestion du token plus simple
- ✅ Historique complet
- ✅ Export/Import de collections
- ✅ Variables d'environnement

---

## 🎊 VOUS ÊTES PRÊT !

Avec Insomnia, vous pouvez tester **toutes les fonctionnalités** de MedVerify facilement !

---

**Créez votre première requête Login maintenant !** 🚀
