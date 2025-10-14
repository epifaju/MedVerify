# 🚀 Guide de Test Rapide - Intégration API-Medicaments.fr

## ⚡ Tests immédiats à effectuer

### 1️⃣ Compilation et démarrage

```bash
# Terminal 1 - Backend
cd medverify-backend
mvn clean install
mvn spring-boot:run

# Attendre le message : "Started MedVerifyApplication"
```

**Vérifier dans les logs :**

```
✅ Flyway - Successfully applied 1 migration to schema medverify (V7)
✅ Started MedVerifyApplication in X.XXX seconds
```

---

### 2️⃣ Test API avec curl/Insomnia

#### a) Se connecter et obtenir le token JWT

```bash
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "votre@email.com",
  "password": "votre_mot_de_passe"
}
```

**Copier le `accessToken` de la réponse**

---

#### b) Test avec médicament français (Doliprane)

```bash
POST http://localhost:8080/api/v1/medications/verify
Authorization: Bearer <VOTRE_TOKEN>
Content-Type: application/json

{
  "gtin": "03400930485088",
  "serialNumber": "TEST001",
  "batchNumber": "LOT2025",
  "expiryDate": "2025-12-31",
  "scannedAt": "2025-10-10T10:00:00Z"
}
```

**Réponse attendue (1er scan) :**

```json
{
  "verificationId": "...",
  "status": "AUTHENTIC",
  "confidence": 1.0,
  "verificationSource": "API_MEDICAMENTS_FR",  ← Important !
  "medication": {
    "name": "DOLIPRANE 1000 mg, comprimé",
    "manufacturer": "SANOFI AVENTIS FRANCE",
    "dosage": "1000 mg",
    ...
  },
  "verifiedAt": "2025-10-10T..."
}
```

---

#### c) Rescanner immédiatement (test cache)

**Même requête, réponse attendue :**

```json
{
  "verificationSource": "CACHE_LOCAL",  ← Cache hit !
  ...
}
```

**Temps de réponse :**

- 1er scan : ~500-1000ms (appel API)
- 2ème scan : ~50-100ms (cache) ⚡

---

### 3️⃣ Vérifier les données en base

```bash
# Connexion PostgreSQL
psql -U postgres -d medverify
```

**Requêtes SQL :**

```sql
-- 1. Voir le médicament ajouté par l'API
SELECT gtin, name, manufacturer, data_source, updated_at
FROM medications
WHERE gtin = '03400930485088';

-- Résultat attendu : data_source = NULL ou 'API_MEDICAMENTS_FR'

-- 2. Voir les statistiques par source
SELECT * FROM medication_sources_stats;

-- 3. Voir les médicaments avec cache expiré
SELECT * FROM expired_cache_medications LIMIT 5;
```

---

### 4️⃣ Vérifier les métriques Prometheus

**Ouvrir dans le navigateur :**

```
http://localhost:8080/actuator/prometheus
```

**Rechercher ces métriques :**

```
medication_cache_hit_total
medication_cache_miss_total
external_api_success_total{provider="api-medicaments-fr"}
external_api_not_found_total{provider="api-medicaments-fr"}
```

---

### 5️⃣ Vérifier les logs

```bash
# Terminal 2
tail -f medverify-backend/logs/medverify.log | grep -E "API-Medicaments|Cache|external.api"
```

**Logs attendus (1er scan) :**

```
INFO : Cache miss or expired, calling external API for GTIN: 03400930485088
INFO : Calling API-Medicaments.fr: https://api-medicaments.fr/medicaments?gtin=03400930485088
INFO : Medication found in API-Medicaments.fr
```

**Logs attendus (2ème scan) :**

```
INFO : Medication found in local cache for GTIN: 03400930485088
```

---

### 6️⃣ Test de l'app mobile (optionnel)

```bash
# Terminal 3 - Frontend
cd MedVerifyApp/MedVerifyExpo
npx expo start
```

**Tester :**

1. Se connecter à l'app
2. Scanner un Data Matrix ou entrer manuellement : `03400930485088`
3. **Vérifier le badge de source** :
   - 1er scan : "🇫🇷 Base française" (bleu)
   - 2ème scan : "📦 Cache local" (vert)

---

## ✅ Checklist de validation

| Test                                                              | Attendu | Résultat |
| ----------------------------------------------------------------- | ------- | -------- |
| Backend démarre sans erreur                                       | ✅      | ☐        |
| Migration V7 appliquée                                            | ✅      | ☐        |
| 1er scan GTIN français → `verificationSource: API_MEDICAMENTS_FR` | ✅      | ☐        |
| 2ème scan → `verificationSource: CACHE_LOCAL`                     | ✅      | ☐        |
| Données en base avec `data_source` renseigné                      | ✅      | ☐        |
| Métriques Prometheus remontées                                    | ✅      | ☐        |
| Logs montrent appels API                                          | ✅      | ☐        |
| Badge source affiché dans l'app mobile                            | ✅      | ☐        |

---

## 🐛 Dépannage rapide

### Problème 1 : Backend ne compile pas

**Erreur :** `setMessage() undefined`

**Solution :**

```bash
mvn clean install -U
# Ou dans votre IDE : Build > Rebuild Project
```

L'annotation `@Data` de Lombok génère les setters à la compilation.

---

### Problème 2 : API-Medicaments.fr ne répond pas

**Vérifier :**

```bash
curl https://api-medicaments.fr/medicaments?gtin=03400930485088
```

**Si timeout ou erreur :**

- L'API peut être temporairement hors ligne
- Le système basculera automatiquement sur la base locale (fallback)
- Vérifier les logs : `external.api.error`

---

### Problème 3 : Médicament non trouvé (404)

**Normal si :**

- GTIN non français
- Médicament retiré du marché
- GTIN invalide

**Le système doit :**

- Retourner `verificationSource: "DB_LOCAL"` (si trouvé en local)
- Ou `verificationSource: "UNKNOWN"` (si non trouvé nulle part)

---

### Problème 4 : Cache ne fonctionne pas

**Vérifier :**

```sql
SELECT * FROM medications WHERE gtin = '03400930485088';
```

**Si `updated_at` est NULL ou très ancien :**

- Le cache est considéré comme expiré
- Vérifier la config : `external-api.cache-ttl-days: 30`

---

## 🧪 Tests avancés

### Test avec API désactivée

1. **Modifier `application.yml` :**

```yaml
external-api:
  medicaments:
    enabled: false
```

2. **Redémarrer le backend**

3. **Scanner un médicament**
   - Devrait retourner `verificationSource: "DB_LOCAL"` directement
   - Aucun appel API dans les logs

---

### Test de timeout

1. **Réduire le timeout à 1ms :**

```yaml
external-api:
  medicaments:
    timeout: 1
```

2. **Scanner un médicament**
   - Devrait timeout immédiatement
   - Fallback sur base locale
   - Métrique `external.api.error` incrémentée

---

### Test de normalisation GTIN

**Tester avec GTIN 13 chiffres :**

```json
{
  "gtin": "3400930485088" // Sans le zéro initial
}
```

**Le système doit automatiquement normaliser à 14 chiffres :**

```
3400930485088 → 03400930485088
```

---

## 📊 Résultats attendus

### Performance

| Métrique                            | Valeur cible |
| ----------------------------------- | ------------ |
| 1er scan (avec API)                 | < 1000ms     |
| Scan suivant (cache)                | < 100ms      |
| Taux de succès API                  | > 95%        |
| Taux de cache hit (après 1 semaine) | > 80%        |

### Disponibilité

| Scénario           | Comportement              |
| ------------------ | ------------------------- |
| API française OK   | ✅ Données à jour         |
| API française down | ✅ Fallback base locale   |
| GTIN non français  | ✅ Base locale uniquement |
| Cache expiré       | ✅ Refresh automatique    |

---

## 🎯 Test final de validation

**Scénario complet :**

1. ✅ Démarrer le backend → Vérifier logs migration V7
2. ✅ Scanner Doliprane (03400930485088) → Badge "🇫🇷 Base française"
3. ✅ Rescanner immédiatement → Badge "📦 Cache local"
4. ✅ Vérifier PostgreSQL → `data_source = 'API_MEDICAMENTS_FR'`
5. ✅ Vérifier métriques → `external_api_success_total > 0`
6. ✅ Désactiver API (`enabled: false`) → Badge "🏥 Base locale"
7. ✅ Réactiver API → Tout fonctionne

**Si tous les tests passent → 🎉 Intégration réussie !**

---

**Temps estimé pour ces tests : 15-20 minutes**

_Bonne chance ! 🚀_




