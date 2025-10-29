# ⚡ Import BDPM - Commandes Rapides

## 🎯 Import 15,803 médicaments en 15 minutes

### **Terminal 1 : Backend**

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\medverify-backend

# Arrêter le backend actuel (Ctrl+C)

# Redémarrer
mvn spring-boot:run

# Attendre : "Started MedVerifyApplication"
# Vérifier : "Current version of schema public: 8" (migration V8 appliquée)
```

---

### **Terminal 2 : Import**

```powershell
# Se connecter
$response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" `
  -Method Post `
  -ContentType "application/json" `
  -Body '{"email":"admin@medverify.gw","password":"Admin123!"}'

$token = $response.accessToken
Write-Host "✅ Token obtenu"

# Lancer l'import
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/admin/bdpm/import" `
  -Method Post `
  -Headers @{"Authorization"="Bearer $token"}

Write-Host "🚀 Import démarré ! Consultez Terminal 3 pour suivre la progression"
```

---

### **Terminal 3 : Logs**

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify\medverify-backend
Get-Content logs\medverify.log -Wait -Tail 30
```

**Logs attendus** :

```
🚀 Starting BDPM full database import...
📊 Total medications: 15803, Pages: 1581
📥 Downloading page 1/1581
📥 Downloading page 2/1581
...
✅ BDPM import completed in 480s
📊 Statistics: 15803 imported, 0 updated, 0 errors
```

---

## ✅ Vérification

```powershell
# Compter les médicaments
$env:PGPASSWORD='Malagueta7'
psql -U postgres -d medverify -c "SELECT COUNT(*) FROM medications WHERE cip13 IS NOT NULL;"

# Vérifier Hélicidine
psql -U postgres -d medverify -c "SELECT gtin, name FROM medications WHERE cip13 = '3400922385624';"
```

---

## 🧪 Test final

**Scanner Hélicidine avec l'app mobile** → Devrait afficher **AUTHENTIC** ✅

---

**⏱️ Durée totale : 15-20 minutes**






