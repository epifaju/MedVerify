# 🎯 API Médicaments Française - Vraie URL et Adaptation

## ✅ API Réelle Trouvée

**URL officielle** : https://medicamentsapi.giygas.dev/

**Documentation** : https://medicamentsapi.giygas.dev/

---

## 📊 Caractéristiques de l'API

- **15,803 médicaments français** (BDPM)
- **Mises à jour** : 2x/jour (6h et 18h)
- **Temps de réponse** : < 100ms
- **Aucune authentification** requise
- **Rate limiting** : 1000 tokens/IP
- **Licence** : Données BDPM, code AGPL v3

---

## 🔍 Endpoints disponibles

| Endpoint                    | Description       | Exemple                   |
| --------------------------- | ----------------- | ------------------------- |
| `GET /medicament/{nom}`     | Recherche par nom | `/medicament/paracetamol` |
| `GET /medicament/id/{cis}`  | Recherche par CIS | `/medicament/id/60904643` |
| `GET /generiques/{libelle}` | Génériques        | `/generiques/paracetamol` |
| `GET /database/{page}`      | Pagination        | `/database/1`             |
| `GET /health`               | Statut API        | `/health`                 |

---

## ⚠️ **PROBLÈME : Pas de recherche par GTIN**

Cette API **NE SUPPORTE PAS** la recherche par GTIN/code-barres !

**Critères de recherche disponibles** :

- ✅ Nom du médicament
- ✅ Code CIS (identifiant français)
- ❌ GTIN (code-barres Data Matrix)

**Exemple de réponse** (contient CIP13, pas GTIN) :

```json
{
  "cis": 60904643,
  "elementPharmaceutique": "CODOLIPRANE 500 mg/30 mg",
  "presentation": [
    {
      "cip13": 3400936403114, // ← CIP13, PAS le GTIN
      "libelle": "...",
      "prix": 3.85
    }
  ]
}
```

---

## 💡 Solutions possibles

### **Solution 1 : Désactiver l'API (ACTUELLE)** ✅

**Configuration actuelle** (`application.yml`) :

```yaml
external-api:
  medicaments:
    base-url: https://medicamentsapi.giygas.dev
    enabled: false # Désactivé car pas de recherche par GTIN
```

**Comportement** :

- Le système utilise **uniquement la base locale** PostgreSQL
- Ajoutez les médicaments manuellement (script SQL fourni)

**Avantages** :

- ✅ Fonctionne immédiatement
- ✅ Pas de dépendance externe
- ✅ Vous contrôlez les données

**Inconvénients** :

- ❌ Base limitée aux médicaments ajoutés manuellement
- ❌ Pas de mise à jour automatique

---

### **Solution 2 : Adapter le client API pour recherche par nom**

Modifier `ApiMedicamentsClient` pour :

1. Extraire le nom du médicament depuis le GTIN (via une table de correspondance)
2. Appeler `/medicament/{nom}`
3. Filtrer les résultats par CIP13

**Code d'exemple** :

```java
public Optional<ApiMedicamentResponse> findByGtin(String gtin) {
    // 1. Convertir GTIN → CIP13 (si possible)
    String cip13 = extractCIP13FromGTIN(gtin);

    // 2. Rechercher dans la base locale pour trouver le nom
    Optional<Medication> localMed = medicationRepository.findByGtin(gtin);
    if (localMed.isEmpty()) {
        return Optional.empty();
    }

    // 3. Appeler l'API par nom
    String url = baseUrl + "/medicament/" + localMed.get().getName();
    // ...
}
```

**Limitations** :

- Nécessite déjà avoir le médicament en base locale
- Complexe et peu fiable

---

### **Solution 3 : Télécharger la base BDPM complète** (MEILLEURE)

L'API permet de télécharger toute la base :

```bash
curl "https://medicamentsapi.giygas.dev/database"
```

**Retourne** : 15,803 médicaments (23MB)

**Implémentation** :

1. Créer un job schedulé (1x/jour)
2. Télécharger la base complète
3. Mettre à jour PostgreSQL

**Avantages** :

- ✅ Base complète de médicaments français
- ✅ Mise à jour automatique
- ✅ Pas de dépendance runtime à l'API
- ✅ Recherche par GTIN si vous créez la correspondance GTIN↔CIS

---

## 🎯 **Recommandation : Solution hybride**

**Phase 1 (Maintenant)** :

- ✅ API désactivée
- ✅ Base locale avec médicaments ajoutés manuellement
- ✅ Système 100% fonctionnel

**Phase 2 (Future)** :

- Créer un job qui télécharge `/database` périodiquement
- Importer dans PostgreSQL
- Créer une table de correspondance GTIN ↔ CIP13 ↔ CIS
- Activer la recherche hybride

---

## 🧪 Test de l'API réelle

**Test 1 : Recherche Hélicidine par nom**

```bash
curl "https://medicamentsapi.giygas.dev/medicament/helicidine"
```

**Test 2 : Recherche Doliprane**

```bash
curl "https://medicamentsapi.giygas.dev/medicament/doliprane"
```

**Test 3 : Health check**

```bash
curl "https://medicamentsapi.giygas.dev/health"
```

**Test 4 : Télécharger base complète (première page)**

```bash
curl "https://medicamentsapi.giygas.dev/database/1"
```

---

## 📝 Configuration finale appliquée

**Fichier** : `application.yml`

```yaml
external-api:
  medicaments:
    base-url: https://medicamentsapi.giygas.dev
    enabled: false # Désactivé - pas de recherche par GTIN
```

**Comportement actuel du système** :

```
Scan → GTIN extrait
  ↓
Cache local (vide)
  ↓
API externe (DÉSACTIVÉE)
  ↓
Base de données locale
  ↓
UNKNOWN (si pas en base) ou AUTHENTIC (si trouvé)
```

---

## 🚀 Actions immédiates

### 1. **Redémarrer le backend** (avec nouvelle config)

Le backend tourne déjà avec l'ancienne configuration. Redémarrez-le :

```bash
# Arrêtez le backend (Ctrl+C dans le terminal)
cd medverify-backend
mvn spring-boot:run
```

### 2. **Ajouter Hélicidine à la base locale**

**Terminal PowerShell** :

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify
$env:PGPASSWORD='Malagueta7'
psql -U postgres -d medverify -f ajout_helicidine_correct.sql
```

### 3. **Scanner Hélicidine**

Résultat attendu :

- ✅ Status: AUTHENTIC
- 🏥 Source: DB_LOCAL
- 💊 Nom: Hélicidine...

---

## 🔮 Future : Import automatique de la base BDPM

**Pour plus tard**, vous pourrez créer un service qui :

1. Télécharge `/database` (toutes les pages)
2. Parse les résultats
3. Importe dans PostgreSQL
4. Crée une correspondance CIP13 ↔ GTIN

**Exemple de code** :

```java
@Scheduled(cron = "0 0 3 * * ?") // 3h du matin
public void importBDPM() {
    // Télécharger /database page par page
    // Parser et insérer dans medications
}
```

---

**Voulez-vous que j'implémente maintenant l'import automatique de la base BDPM complète, ou préférez-vous d'abord tester avec Hélicidine en base locale ?**


