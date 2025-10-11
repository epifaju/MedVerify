# 🔥 Commandes de Test Prêtes à l'Emploi

## Variables à définir

```bash
# Définir votre token JWT (après login)
export JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Ou définir email/password pour login automatique
export API_EMAIL="admin@medverify.gw"
export API_PASSWORD="Admin123!"
```

---

## 1. Login et récupération du token

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$API_EMAIL\",
    \"password\": \"$API_PASSWORD\"
  }" | jq -r '.accessToken'
```

**Copier le token et l'exporter :**

```bash
export JWT_TOKEN="<token_copié>"
```

---

## 2. Test avec médicament français (Doliprane)

### Premier scan (devrait appeler l'API)

```bash
curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "03400930485088",
    "serialNumber": "TEST001",
    "batchNumber": "LOT2025",
    "expiryDate": "2025-12-31",
    "scannedAt": "2025-10-10T10:00:00Z"
  }' | jq '.verificationSource'
```

**Résultat attendu :** `"API_MEDICAMENTS_FR"`

---

### Deuxième scan (devrait utiliser le cache)

```bash
curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "03400930485088",
    "serialNumber": "TEST002",
    "batchNumber": "LOT2025",
    "expiryDate": "2025-12-31",
    "scannedAt": "2025-10-10T10:01:00Z"
  }' | jq '.verificationSource'
```

**Résultat attendu :** `"CACHE_LOCAL"`

---

## 3. Test avec GTIN 13 chiffres (normalisation)

```bash
curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "3400930485088",
    "serialNumber": "TEST003",
    "batchNumber": "LOT2025",
    "expiryDate": "2025-12-31",
    "scannedAt": "2025-10-10T10:02:00Z"
  }' | jq '{verificationSource, gtin: .medication.gtin}'
```

**Résultat attendu :**

```json
{
  "verificationSource": "CACHE_LOCAL",
  "gtin": "03400930485088"
}
```

---

## 4. Test avec GTIN invalide

```bash
curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "99999999999999",
    "serialNumber": "TEST004",
    "batchNumber": "LOT2025",
    "expiryDate": "2025-12-31",
    "scannedAt": "2025-10-10T10:03:00Z"
  }' | jq '{status, verificationSource, message}'
```

**Résultat attendu :**

```json
{
  "status": "UNKNOWN",
  "verificationSource": "NONE",
  "message": "Médicament non trouvé dans les bases de données françaises et locales"
}
```

---

## 5. Autres médicaments français à tester

### Efferalgan 1000mg

```bash
curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "03400936646506",
    "serialNumber": "TEST005",
    "scannedAt": "2025-10-10T10:04:00Z"
  }' | jq '{name: .medication.name, source: .verificationSource}'
```

### Dafalgan 500mg

```bash
curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "03400930007839",
    "serialNumber": "TEST006",
    "scannedAt": "2025-10-10T10:05:00Z"
  }' | jq '{name: .medication.name, source: .verificationSource}'
```

---

## 6. Vérifier les métriques Prometheus

```bash
# Toutes les métriques
curl http://localhost:8080/actuator/prometheus

# Filtrer les métriques d'API externe
curl http://localhost:8080/actuator/prometheus | grep external_api

# Filtrer les métriques de cache
curl http://localhost:8080/actuator/prometheus | grep medication_cache
```

---

## 7. Vérifier les statistiques en base

```bash
# Connexion PostgreSQL
psql -U postgres -d medverify -c "SELECT * FROM medication_sources_stats;"

# Médicaments récemment synchronisés
psql -U postgres -d medverify -c "
  SELECT gtin, name, data_source, updated_at
  FROM medications
  WHERE data_source = 'API_MEDICAMENTS_FR'
  ORDER BY updated_at DESC
  LIMIT 5;
"

# Cache expiré
psql -U postgres -d medverify -c "SELECT * FROM expired_cache_medications LIMIT 5;"
```

---

## 8. Tests de performance

### Mesurer temps de réponse (1er scan)

```bash
time curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "03400930485088",
    "serialNumber": "PERF001",
    "scannedAt": "2025-10-10T10:06:00Z"
  }' -s | jq '.verificationSource'
```

**Temps attendu :** ~0.5-1.0 secondes (avec appel API)

---

### Mesurer temps de réponse (cache hit)

```bash
time curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "03400930485088",
    "serialNumber": "PERF002",
    "scannedAt": "2025-10-10T10:07:00Z"
  }' -s | jq '.verificationSource'
```

**Temps attendu :** ~0.05-0.1 secondes (cache) ⚡

---

## 9. Test de charge (optionnel)

### 10 scans consécutifs

```bash
for i in {1..10}; do
  echo "Scan #$i"
  curl -X POST http://localhost:8080/api/v1/medications/verify \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"gtin\": \"03400930485088\",
      \"serialNumber\": \"LOAD${i}\",
      \"scannedAt\": \"2025-10-10T10:0${i}:00Z\"
    }" -s | jq -r '.verificationSource'
  sleep 0.5
done
```

**Résultats attendus :**

```
Scan #1: API_MEDICAMENTS_FR  (ou CACHE_LOCAL si déjà en cache)
Scan #2: CACHE_LOCAL
Scan #3: CACHE_LOCAL
...
Scan #10: CACHE_LOCAL
```

---

## 10. Tests d'erreurs

### Test avec JWT invalide

```bash
curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer INVALID_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "03400930485088"
  }' | jq
```

**Résultat attendu :** HTTP 401 Unauthorized

---

### Test avec GTIN manquant

```bash
curl -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "serialNumber": "TEST007"
  }' | jq
```

**Résultat attendu :** HTTP 400 Bad Request

---

## 11. Commande tout-en-un de test complet

```bash
#!/bin/bash
# test_integration_api.sh

echo "🚀 Test d'intégration API-Medicaments.fr"
echo "=========================================="
echo ""

# 1. Login
echo "1️⃣ Login..."
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$API_EMAIL\", \"password\": \"$API_PASSWORD\"}" \
  | jq -r '.accessToken')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
  echo "❌ Erreur login"
  exit 1
fi
echo "✅ Token obtenu"
echo ""

# 2. Premier scan (API)
echo "2️⃣ Premier scan (devrait appeler API)..."
SOURCE1=$(curl -s -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "03400930485088",
    "serialNumber": "TEST001",
    "scannedAt": "2025-10-10T10:00:00Z"
  }' | jq -r '.verificationSource')

echo "   Source: $SOURCE1"
if [ "$SOURCE1" == "API_MEDICAMENTS_FR" ] || [ "$SOURCE1" == "CACHE_LOCAL" ]; then
  echo "✅ OK"
else
  echo "⚠️  Attendu: API_MEDICAMENTS_FR ou CACHE_LOCAL"
fi
echo ""

# 3. Deuxième scan (Cache)
echo "3️⃣ Deuxième scan (devrait utiliser cache)..."
SOURCE2=$(curl -s -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "03400930485088",
    "serialNumber": "TEST002",
    "scannedAt": "2025-10-10T10:01:00Z"
  }' | jq -r '.verificationSource')

echo "   Source: $SOURCE2"
if [ "$SOURCE2" == "CACHE_LOCAL" ]; then
  echo "✅ OK - Cache fonctionne !"
else
  echo "⚠️  Attendu: CACHE_LOCAL"
fi
echo ""

# 4. Test GTIN invalide
echo "4️⃣ Test GTIN invalide..."
STATUS=$(curl -s -X POST http://localhost:8080/api/v1/medications/verify \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "gtin": "99999999999999",
    "serialNumber": "TEST003",
    "scannedAt": "2025-10-10T10:02:00Z"
  }' | jq -r '.status')

echo "   Status: $STATUS"
if [ "$STATUS" == "UNKNOWN" ]; then
  echo "✅ OK - Gestion erreur correcte"
else
  echo "⚠️  Attendu: UNKNOWN"
fi
echo ""

# 5. Métriques
echo "5️⃣ Vérification métriques..."
METRICS=$(curl -s http://localhost:8080/actuator/prometheus | grep -c "external_api_success_total")
if [ $METRICS -gt 0 ]; then
  echo "✅ Métriques présentes"
else
  echo "⚠️  Métriques non trouvées"
fi
echo ""

echo "=========================================="
echo "🎉 Tests terminés !"
```

**Rendre exécutable et lancer :**

```bash
chmod +x test_integration_api.sh
./test_integration_api.sh
```

---

## 💡 Tips

### Formater joliment le JSON

```bash
# Installer jq si pas déjà fait
# Linux/Mac: brew install jq
# Windows: choco install jq

# Utiliser avec curl
curl ... | jq '.'
```

### Sauvegarder les réponses

```bash
curl ... | jq '.' > response.json
```

### Mesurer uniquement le temps HTTP

```bash
curl -w "\nTemps: %{time_total}s\n" -o /dev/null -s ...
```

---

**🚀 Vous êtes prêt à tester l'intégration !**


