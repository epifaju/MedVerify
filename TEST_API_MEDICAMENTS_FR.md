# 🧪 Test de l'API-Medicaments.fr - Vérification

## 🎯 Objectif

Vérifier que l'API française des médicaments fonctionne correctement avant l'intégration complète.

---

## 📝 Informations API fournies

- **URL principale** : https://api.opendata.fr/datasets/base-de-donnees-publique-des-medicaments
- **Documentation** : https://api-medicaments.fr/
- **Endpoint exemple** : `GET https://api-medicaments.fr/medicaments?gtin=XXXXXXXXXXXXXX`
- **Source officielle** : https://www.data.gouv.fr/fr/datasets/base-de-donnees-publique-des-medicaments/

---

## 🧪 Tests manuels de l'API

### Test 1 : Vérifier la documentation

**Ouvrir dans le navigateur :**

```
https://api-medicaments.fr/
```

**Attendu :** Page de documentation avec exemples d'utilisation

---

### Test 2 : Test direct avec curl (Hélicidine)

```bash
curl -X GET "https://api-medicaments.fr/medicaments?gtin=03400922385624" \
  -H "Accept: application/json" \
  -H "User-Agent: MedVerify/1.0" \
  -v
```

**Vérifier :**

- Status HTTP : doit être 200 ou 404
- Content-Type : doit être `application/json`
- Body : doit être du JSON (pas du HTML)

---

### Test 3 : Test avec un GTIN Doliprane (connu)

```bash
curl -X GET "https://api-medicaments.fr/medicaments?gtin=03400930485088" \
  -H "Accept: application/json" \
  -H "User-Agent: MedVerify/1.0"
```

**Réponse attendue (si l'API fonctionne) :**

```json
[
  {
    "code": "03400930485088",
    "denomination": "DOLIPRANE 1000 mg, comprimé",
    "forme": "comprimé",
    "dosage": "1000 mg",
    "laboratoire": "SANOFI AVENTIS FRANCE",
    "statut": "Autorisée",
    ...
  }
]
```

---

## 🔧 Configuration actuelle du backend

Fichier : `medverify-backend/src/main/resources/application.yml`

```yaml
external-api:
  medicaments:
    base-url: https://api-medicaments.fr
    timeout: 5000
    enabled: true # ← Actuellement activé
```

---

## ⚠️ Si l'API ne fonctionne pas encore

### Option 1 : Désactiver temporairement l'API

Modifiez `application.yml` :

```yaml
external-api:
  medicaments:
    enabled: false # ← Désactivé temporairement
```

**Effet :** Le système utilisera **uniquement la base locale** (pas d'appels API)

---

### Option 2 : Ajouter les médicaments français manuellement

Pour l'instant, ajoutez les médicaments français que vous scannez dans votre base locale PostgreSQL.

**Script fourni :** `ajout_helicidine_correct.sql`

**Exécution :**

```powershell
# Ouvrir un nouveau PowerShell
cd C:\Users\epifa\cursor-workspace\MedVerify
$env:PGPASSWORD='Malagueta7'
psql -U postgres -d medverify -f ajout_helicidine_correct.sql
```

---

## 🚀 Modifications apportées au backend

### 1. **Headers HTTP ajoutés** ✅

Le client API envoie maintenant :

```
Accept: application/json
Content-Type: application/json
User-Agent: MedVerify/1.0
```

Cela force l'API à retourner du JSON au lieu de HTML.

### 2. **Gestion d'erreur améliorée** ✅

Si l'API retourne du HTML ou une erreur :

- ✅ Fallback automatique sur base locale
- ✅ Métriques enregistrées (`external.api.error`)
- ✅ Logs clairs pour debugging

---

## 📊 Prochaines étapes

### 1. **Tester l'API manuellement**

Avant de relancer le backend, testez l'API avec curl ou Postman pour vérifier qu'elle répond bien en JSON.

### 2. **Recompiler le backend**

```bash
cd medverify-backend
mvn clean install
```

### 3. **Redémarrer le backend**

```bash
mvn spring-boot:run
```

### 4. **Rescanner Hélicidine**

Avec l'app mobile, rescannez le code. Vérifiez les logs backend :

**Si l'API fonctionne :**

```
INFO : Calling API-Medicaments.fr: https://api-medicaments.fr/medicaments?gtin=03400922385624
INFO : Medication found in API-Medicaments.fr
```

**Si l'API échoue (HTML reçu) :**

```
ERROR : External API error, falling back to local DB
```

---

## 🔍 Diagnostic de l'erreur actuelle

**Erreur vue dans les logs :**

```
Could not extract response: no suitable HttpMessageConverter
found for response type [...] and content type [text/html;charset=utf-8]
```

**Cause :** L'API retourne du HTML au lieu de JSON

**Solutions possibles :**

1. ✅ **Headers ajoutés** (Accept: application/json) - FAIT
2. 🔍 **Vérifier que l'URL de l'API est correcte**
3. 🔍 **L'API peut être temporairement hors ligne**
4. 🔍 **L'API peut nécessiter une clé d'authentification**

---

## 💡 Recommandation

**Pour tester immédiatement votre système :**

1. **Désactivez l'API temporairement** :

   ```yaml
   external-api:
     medicaments:
       enabled: false
   ```

2. **Ajoutez Hélicidine manuellement** avec le script SQL

3. **Testez le scan** → Devrait afficher "AUTHENTIC" ✅

4. **En parallèle, testez l'API avec curl** pour vérifier qu'elle fonctionne

5. **Réactivez l'API** quand confirmée fonctionnelle

---

**Que souhaitez-vous faire en priorité ?**

A. Désactiver l'API et tester avec base locale uniquement  
B. Investiguer pourquoi l'API retourne du HTML  
C. Ajouter Hélicidine manuellement pour tester le scan  
D. Tout à la fois (désactiver API + ajouter Hélicidine)


