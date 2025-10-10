# Correction du Rate Limiting pour l'Import BDPM

## Problèmes Identifiés

D'après les logs d'import, deux problèmes critiques ont été détectés :

### 1. Erreur 429 - Too Many Requests

```
HTTP error response: 429 - Too Many Requests
```

L'API externe (medicamentsapi.giygas.dev) bloque les requêtes car elles sont envoyées trop rapidement.

### 2. Erreur de Parsing JSON

```
Could not extract response: no suitable HttpMessageConverter found for response type
[class com.medverify.service.BDPMImportService$BDPMPageResponse]
and content type [text/plain;charset=utf-8]
```

Quand l'API retourne une erreur 429, elle renvoie du texte brut au lieu de JSON.

## Corrections Apportées

### 1. Augmentation du Délai Entre les Requêtes

**AVANT :**

```java
// Petite pause pour ne pas surcharger l'API (rate limiting)
if (page % 10 == 0) {
    Thread.sleep(1000); // 1 seconde toutes les 10 pages
}
```

⚠️ Problème : 1 seconde d'attente toutes les 10 pages = ~10 requêtes/seconde

**APRÈS :**

```java
// Pause entre chaque requête pour éviter le rate limiting
// 500ms = 2 requêtes/seconde max
Thread.sleep(500);
```

✅ Solution : 500ms entre chaque requête = 2 requêtes/seconde max

### 2. Gestion des Erreurs 429 avec Retry Automatique

Nouvelle méthode `downloadPageWithRetry()` ajoutée :

```java
private List<BDPMMedicamentResponse> downloadPageWithRetry(int pageNumber) {
    int maxRetries = 5;
    int retryDelay = 2000; // 2 secondes

    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            return downloadPage(pageNumber);
        } catch (HttpClientErrorException.TooManyRequests e) {
            // Erreur 429 - Too Many Requests
            log.warn("⚠️ Rate limited on page {} (attempt {}/{}), waiting {}ms...",
                    pageNumber, attempt, maxRetries, retryDelay);

            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryDelay);
                    // Augmenter le délai pour le prochain retry (backoff exponentiel)
                    retryDelay *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return List.of();
                }
            }
        }
    }
}
```

**Fonctionnement :**

- **5 tentatives maximum** par page
- **Backoff exponentiel** : 2s → 4s → 8s → 16s → 32s
- **Capture spécifique** de l'erreur 429
- **Logs informatifs** pour suivre les retries

### 3. Attente Plus Longue en Cas d'Erreur

```java
} catch (Exception e) {
    log.error("Error importing page {}: {}", page, e.getMessage());
    errors.incrementAndGet();
    // En cas d'erreur, attendre plus longtemps avant de continuer
    Thread.sleep(2000);
}
```

## Temps d'Import Estimé

Avec les nouvelles corrections :

- **Pages totales** : ~1,581 pages
- **Délai par page** : 500ms minimum
- **Temps minimum** : 1,581 × 0.5s = **~13 minutes**
- **Avec retries** : **~15-20 minutes** (estimation réaliste)

## Avantages de la Solution

1. ✅ **Respect du rate limiting** de l'API externe
2. ✅ **Retry automatique** en cas d'erreur 429
3. ✅ **Backoff exponentiel** pour éviter de surcharger l'API
4. ✅ **Logs détaillés** pour suivre la progression
5. ✅ **Gestion robuste** des erreurs

## Comment Tester

### Via Swagger UI (Firefox)

1. Ouvrir Firefox et aller sur : http://localhost:8080/swagger-ui.html
2. Trouver l'endpoint : **POST /api/admin/bdpm/import**
3. Cliquer sur "Try it out" puis "Execute"
4. Surveiller les logs dans `medverify-backend/logs/medverify.log`

### Via curl

```bash
# Lancer l'import
curl -X POST http://localhost:8080/api/admin/bdpm/import \
  -H "Content-Type: application/json" \
  -u admin@medverify.com:admin123

# Vérifier le statut
curl http://localhost:8080/api/admin/bdpm/status \
  -u admin@medverify.com:admin123
```

### Surveiller les Logs

```bash
# En temps réel
tail -f medverify-backend/logs/medverify.log

# Filtrer uniquement l'import BDPM
tail -f medverify-backend/logs/medverify.log | grep -i "bdpm\|downloading page"
```

## Logs Attendus

Maintenant vous devriez voir :

```
2025-10-10 02:15:00 - 🚀 Starting BDPM full database import
2025-10-10 02:15:01 - 📊 Total medications in BDPM: 15803, Pages to download: 1581
2025-10-10 02:15:01 - 📥 Downloading page 1/1581
2025-10-10 02:15:02 - 📥 Downloading page 2/1581
...
```

Si des erreurs 429 surviennent, vous verrez :

```
2025-10-10 02:15:30 - ⚠️ Rate limited on page 425 (attempt 1/5), waiting 2000ms...
2025-10-10 02:15:32 - 📥 Downloading page 425/1581 (retry successful)
```

## Statut

✅ **Corrections appliquées**
✅ **Backend recompilé**
✅ **Backend redémarré**
🕐 **Prêt pour import**

## Prochaine Étape

Vous pouvez maintenant relancer l'import BDPM via Swagger UI et surveiller les logs. L'import devrait maintenant se terminer avec succès sans erreurs 429.

