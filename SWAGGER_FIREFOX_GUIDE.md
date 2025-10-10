# 🔐 Guide Swagger + Firefox - MedVerify

## 🎯 LOCALISER LE BOUTON AUTHORIZE

### Option 1 : Recherche Ctrl+F

1. Sur la page Swagger : **`Ctrl + F`**
2. Tapez : **`Authorize`**
3. Firefox surligne le bouton en **jaune**
4. Cliquez dessus !

### Option 2 : Rechargement Complet

1. **`Ctrl + Shift + R`** (ou `Ctrl + F5`)
2. Attendez que la page charge complètement
3. Cherchez en **haut à droite** : un bouton **"Authorize"** ou un **cadenas** 🔒

### Option 3 : Vérifier la Barre de Défilement

Parfois le bouton est **tout en haut** de la page :

1. Scrollez **complètement vers le haut**
2. Le bouton est souvent juste sous le titre "MedVerify API v1.0.0"

---

## 🔧 MÉTHODE ALTERNATIVE : Console Firefox

### 1. Login via Swagger

`POST /api/v1/auth/login` :

```json
{
  "email": "admin@medverify.gw",
  "password": "Admin@123456"
}
```

**Copiez** le `accessToken` de la réponse.

### 2. Ouvrir Developer Tools

**`F12`** → Onglet **Console**

### 3. Tester la Vérification

Copiez-collez ce code (remplacez `VOTRE_TOKEN`) :

```javascript
// 1. D'abord, stockez votre token
const token = "VOTRE_TOKEN_ICI";

// 2. Test de vérification
fetch("http://localhost:8080/api/v1/medications/verify", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  },
  body: JSON.stringify({
    gtin: "03401234567890",
    serialNumber: "TEST123",
    batchNumber: "LOT2024A123",
  }),
})
  .then((res) => res.json())
  .then((data) => {
    console.log("✅ Résultat :", data);
    return data;
  });
```

### 4. Autres Tests

**Dashboard** :

```javascript
fetch("http://localhost:8080/api/v1/admin/dashboard/stats?period=30d", {
  method: "GET",
  headers: {
    Authorization: `Bearer ${token}`,
  },
})
  .then((res) => res.json())
  .then((data) => console.log("📊 Dashboard :", data));
```

**Créer un Signalement** :

```javascript
fetch("http://localhost:8080/api/v1/reports", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  },
  body: JSON.stringify({
    gtin: "03401234567890",
    reportType: "COUNTERFEIT",
    description: "Test de signalement via console Firefox",
  }),
})
  .then((res) => res.json())
  .then((data) => console.log("📢 Signalement :", data));
```

---

## 🚀 MÉTHODE ULTRA-SIMPLE : Extension RESTClient Firefox

### Installer RESTClient

1. Menu Firefox → **Add-ons** (ou `Ctrl + Shift + A`)
2. Cherchez : **"RESTClient"**
3. Installez l'extension

### Utiliser RESTClient

1. **Menu Tools** → **RESTClient**
2. **Method** : POST
3. **URL** : `http://localhost:8080/api/v1/auth/login`
4. **Body** :
   ```json
   {
     "email": "admin@medverify.gw",
     "password": "Admin@123456"
   }
   ```
5. **Headers** → Add : `Content-Type: application/json`
6. **Send**
7. **Copiez** le token
8. **Nouvelle requête** avec header : `Authorization: Bearer TOKEN`

---

## 📊 SI SWAGGER NE FONCTIONNE PAS DU TOUT

### Alternative : Utiliser curl dans PowerShell

**Login** :

```powershell
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"admin@medverify.gw\",\"password\":\"Admin@123456\"}'
```

**Verify** (avec token) :

```powershell
curl -X POST http://localhost:8080/api/v1/medications/verify `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer VOTRE_TOKEN" `
  -d '{\"gtin\":\"03401234567890\",\"serialNumber\":\"TEST123\",\"batchNumber\":\"LOT2024A123\"}'
```

---

## 🎯 DIAGNOSTIC

### Vérifier que Swagger Fonctionne

1. Ouvrez : http://localhost:8080/swagger-ui.html
2. Vous devez voir : **"MedVerify API"** comme titre
3. **Plusieurs sections** : Authentication, Medications, Reports, etc.

### Si Swagger est Vide

Le JavaScript ne se charge pas :

1. **Désactivez** les bloqueurs de publicités (uBlock, AdBlock)
2. **Autorisez** JavaScript pour localhost
3. **Rechargez** : `Ctrl + Shift + R`

### Si Swagger Affiche une Erreur

Backend non configuré correctement :

1. Vérifiez : http://localhost:8080/api-docs
2. Doit retourner du JSON (pas d'erreur 404)

---

## ✅ SOLUTION RECOMMANDÉE

**Pour tester rapidement MAINTENANT** :

1. **F12** → Console Firefox
2. **Collez** :
   ```javascript
   // Login
   fetch("http://localhost:8080/api/v1/auth/login", {
     method: "POST",
     headers: { "Content-Type": "application/json" },
     body: JSON.stringify({
       email: "admin@medverify.gw",
       password: "Admin@123456",
     }),
   })
     .then((res) => res.json())
     .then((data) => {
       console.log("Token:", data.accessToken);
       // Stockez le token
       window.token = data.accessToken;
     });
   ```
3. **Copiez** le token affiché
4. **Testez** :
   ```javascript
   // Verify
   fetch("http://localhost:8080/api/v1/medications/verify", {
     method: "POST",
     headers: {
       "Content-Type": "application/json",
       Authorization: `Bearer ${window.token}`,
     },
     body: JSON.stringify({
       gtin: "03401234567890",
       serialNumber: "TEST123",
       batchNumber: "LOT2024A123",
     }),
   })
     .then((res) => res.json())
     .then((data) => console.log("Résultat:", data));
   ```

---

**C'est la méthode la plus rapide pour tester MAINTENANT !** 🚀
