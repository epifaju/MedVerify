# Relancer l'Import BDPM avec les Corrections

## ✅ Corrections Appliquées

Les problèmes de rate limiting (erreur 429) ont été corrigés :

1. **Délai augmenté** : 500ms entre chaque requête (au lieu de 1s toutes les 10 pages)
2. **Retry automatique** : 5 tentatives avec backoff exponentiel
3. **Gestion robuste** des erreurs 429

## 🚀 Relancer l'Import

### Option 1 : Via Swagger UI (Recommandé)

1. **Ouvrir Firefox** et aller sur :

   ```
   http://localhost:8080/swagger-ui.html
   ```

2. **Trouver** : `admin-controller` → `POST /api/admin/bdpm/import`

3. **Cliquer** sur "Try it out"

4. **Execute**

5. **Surveiller les logs** :
   ```powershell
   Get-Content medverify-backend\logs\medverify.log -Wait -Tail 50
   ```

### Option 2 : Via curl

```bash
curl -X POST http://localhost:8080/api/admin/bdpm/import \
  -H "Content-Type: application/json" \
  -u admin@medverify.com:admin123
```

### Option 3 : Via PowerShell

```powershell
$user = "admin@medverify.com"
$pass = "admin123"
$pair = "$($user):$($pass)"
$encodedCreds = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes($pair))
$headers = @{
    Authorization = "Basic $encodedCreds"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/admin/bdpm/import" -Method Post -Headers $headers
```

## 📊 Surveiller la Progression

### Voir les logs en temps réel :

```powershell
Get-Content medverify-backend\logs\medverify.log -Wait -Tail 50
```

### Filtrer uniquement l'import :

```powershell
Get-Content medverify-backend\logs\medverify.log -Wait | Select-String "Downloading page"
```

## 📈 Ce que vous verrez

### Démarrage :

```
🚀 Starting BDPM full database import
📊 Total medications in BDPM: 15803, Pages to download: 1581
```

### Progression normale :

```
📥 Downloading page 1/1581
📥 Downloading page 2/1581
📥 Downloading page 3/1581
...
```

### En cas de rate limiting (maintenant géré !) :

```
⚠️ Rate limited on page 425 (attempt 1/5), waiting 2000ms...
📥 Downloading page 425/1581 (retry successful)
```

### Completion :

```
✅ BDPM import completed in XXXs
📊 Statistics: 15803 imported, 0 updated, 0 errors
```

## ⏱️ Temps Estimé

- **Minimum** : ~13 minutes
- **Avec retries** : ~15-20 minutes
- **Total pages** : 1,581 pages
- **Vitesse** : 2 requêtes/seconde maximum

## 🔍 Vérifier le Résultat

Une fois terminé, vérifier le nombre de médicaments importés :

```bash
curl http://localhost:8080/api/admin/bdpm/status \
  -u admin@medverify.com:admin123
```

Ou via Swagger UI :

```
GET /api/admin/bdpm/status
```

## ⚠️ Remarques Importantes

1. **Ne pas interrompre** l'import en cours
2. **L'import prendra du temps** (15-20 minutes) - c'est normal !
3. **Les erreurs 429 sont gérées** automatiquement avec retry
4. **Les logs sont détaillés** pour suivre la progression

## 🆘 En Cas de Problème

### L'import ne démarre pas :

Vérifier que le backend est démarré :

```bash
curl http://localhost:8080/actuator/health
```

### Erreurs persistantes :

Consulter les logs complets :

```powershell
Get-Content medverify-backend\logs\medverify.log
```

### Backend ne répond pas :

Redémarrer le backend :

```powershell
cd medverify-backend
java -jar target\medverify-backend-1.0.0-SNAPSHOT.jar
```

## 📋 Checklist

- [x] Backend démarré (http://localhost:8080)
- [x] Corrections appliquées (rate limiting)
- [x] Prêt pour l'import
- [ ] Import lancé via Swagger/curl
- [ ] Logs surveillés en temps réel
- [ ] Import terminé avec succès
- [ ] Médicaments importés vérifiés

---

**Status actuel** : ✅ **Prêt pour l'import !**


