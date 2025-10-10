# 🔍 Debug Erreur 500 - Login Insomnia

## 🚨 Problème

Erreur 500 lors du login via Insomnia :

```json
{
  "error": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred",
  "timestamp": "2025-10-09T19:18:19.548444900Z"
}
```

## 🔧 DIAGNOSTIC

### Vérification 1 : Logs Backend

Dans le terminal backend, cherchez l'erreur autour de **21:18:19**.

Vous devriez voir une **stacktrace** avec l'erreur exacte.

### Vérification 2 : Format de la Requête Insomnia

Vérifiez que votre requête Insomnia est correctement formatée :

**Method** : POST  
**URL** : `http://localhost:8080/api/v1/auth/login`  
**Headers** :

- `Content-Type: application/json`

**Body** (JSON) :

```json
{
  "email": "admin@medverify.gw",
  "password": "Admin@123456"
}
```

---

## ✅ SOLUTION RAPIDE : Tester avec curl

Pour vérifier si le problème vient d'Insomnia ou du backend :

```powershell
curl -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@medverify.gw\",\"password\":\"Admin@123456\"}"
```

**Si curl fonctionne** → Problème Insomnia (mauvais format)  
**Si curl échoue aussi** → Problème backend

---

## 🔍 CAUSES POSSIBLES

### 1. Champs Manquants

Le backend attend peut-être d'autres champs. Essayez avec tous les champs :

```json
{
  "email": "admin@medverify.gw",
  "password": "Admin@123456"
}
```

### 2. Headers Incorrects

Vérifiez dans Insomnia que :

- ✅ `Content-Type: application/json` est bien présent
- ✅ Pas de header `Authorization` sur le login (c'est pour après)

### 3. Problème de Parsing JSON

Certaines versions d'Insomnia ont des bugs. Essayez :

- **Body** → **Raw** → Type : **JSON**
- Pas de guillemets supplémentaires

---

## 🚀 SOLUTION DE CONTOURNEMENT

Si Insomnia pose problème, utilisez **Firefox Console** (très simple) :

### 1. Ouvrir Firefox Console

1. Ouvrez Firefox : http://localhost:8080
2. **`F12`** → Onglet **Console**

### 2. Exécuter le Login

Copiez-collez ce code :

```javascript
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
    if (data.accessToken) {
      console.log("✅ LOGIN RÉUSSI !");
      console.log("Token:", data.accessToken);
      window.token = data.accessToken;
    } else {
      console.log("❌ Erreur:", data);
    }
  });
```

### 3. Tester la Vérification

```javascript
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
  .then((data) => console.log("🎉 Résultat:", data));
```

---

## 📋 ACTIONS IMMÉDIATES

### Pour Identifier le Problème

1. **Regardez les logs backend** au moment de l'erreur 21:18:19
2. Cherchez une ligne avec **"ERROR"** ou une stacktrace
3. **Copiez-moi** l'erreur exacte

### Tester Rapidement

**Option A** : Utilisez **Firefox Console** (code ci-dessus)  
**Option B** : Testez avec **curl** dans PowerShell

---

## 🎯 QUESTIONS

**Dites-moi** :

1. Que voyez-vous dans les logs backend à **21:18:19** ?
2. Dans Insomnia, quel **Body Type** avez-vous sélectionné ? (JSON, Raw, Form...)
3. Voulez-vous essayer avec Firefox Console à la place ?

Je vais vous aider à identifier et résoudre le problème ! 😊
