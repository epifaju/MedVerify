# 🔧 Guide Complet : Corriger les médicaments SUSPECT

## 🎯 Pourquoi les médicaments sont marqués SUSPECT ?

### Algorithme de vérification

L'algorithme utilise un **système de score de confiance** :

```
Score initial : 1.0 (100%)

Règle 1 : isActive = false       → -0.5
Règle 2 : Série dupliquée ≥5 fois → -0.6
Règle 3 : Périmé                 → -0.3
Règle 4 : Lot rappelé            → -0.8

Seuil de confiance : 0.7

✅ Score ≥ 0.7 → AUTHENTIC
⚠️ Score < 0.7 → SUSPICIOUS
```

### Exemple concret :

**Médicament avec `isActive = false`** :

- Score = 1.0 - 0.5 = **0.5**
- 0.5 < 0.7 → **SUSPICIOUS** ⚠️

**Médicament avec `isActive = true`** :

- Score = **1.0**
- 1.0 ≥ 0.7 → **AUTHENTIC** ✅

---

## ✅ SOLUTION 1 : Script PowerShell automatique (⭐ RECOMMANDÉ)

### Étape 1 : Modifier le mot de passe

Ouvrez `activer_tous_medicaments.ps1` et **remplacez** :

```powershell
password = "VOTRE_MOT_DE_PASSE_ICI"
```

Par votre vrai mot de passe :

```powershell
password = "votre_mot_de_passe_réel"
```

### Étape 2 : Exécuter le script

**Double-cliquez** sur `activer_tous_medicaments.ps1`

OU dans PowerShell :

```powershell
.\activer_tous_medicaments.ps1
```

### Résultat attendu :

```
✅ X médicaments activés avec succès
```

---

## ✅ SOLUTION 2 : Via Swagger avec authentification (3 minutes)

### Étape 1 : Se connecter

1. Ouvrez : `http://192.168.1.16:8080/swagger-ui.html`
2. Trouvez `POST /api/v1/auth/login`
3. Cliquez "Try it out"
4. Entrez vos identifiants :

```json
{
  "email": "epifaju@admin.com",
  "password": "votre_mot_de_passe"
}
```

5. Cliquez "Execute"
6. **COPIEZ** le `accessToken` (long texte)

### Étape 2 : Autoriser Swagger

1. **En haut à droite**, cliquez sur **"Authorize" 🔓**
2. Dans "Value", entrez :

```
Bearer eyJhbGciOiJI... (collez votre token complet)
```

3. Cliquez "Authorize"
4. Fermez

### Étape 3 : Activer les médicaments

1. Trouvez `POST /api/v1/medications/admin/activate-all`
2. Cliquez "Try it out"
3. Cliquez "Execute"
4. Vérifiez la réponse : `✅ X médicaments activés`

---

## ✅ SOLUTION 3 : Via SQL direct (si vous avez un outil SQL)

### Si vous avez pgAdmin, DBeaver, HeidiSQL, etc. :

1. Connectez-vous à :

   - Host : `localhost`
   - Port : `5432`
   - Database : `medverify`
   - User : `postgres`
   - Password : `Malagueta7`

2. Exécutez :

```sql
-- Activer TOUS les médicaments
UPDATE medications SET is_active = true, updated_at = NOW();

-- Vérifier le résultat
SELECT COUNT(*) FROM medications WHERE is_active = true;
```

---

## ✅ SOLUTION 4 : Modifier le seuil (alternative)

### Si vous ne voulez pas modifier la base de données :

Modifiez `medverify-backend/src/main/resources/application.yml` :

```yaml
app:
  verification:
    confidence-threshold: 0.5 # Au lieu de 0.7
```

**Puis redémarrez le backend.**

Avec ce changement :

- Médicaments inactifs (score 0.5) → **AUTHENTIC** ✅
- ⚠️ Mais moins strict sur la sécurité

---

## 📊 Comparaison des solutions

| Solution              | Difficulté | Temps  | Impact                          |
| --------------------- | ---------- | ------ | ------------------------------- |
| **Script PowerShell** | ⭐ Facile  | 30 sec | ✅ Corrige tous les médicaments |
| **Swagger auth**      | ⭐⭐ Moyen | 3 min  | ✅ Corrige tous les médicaments |
| **SQL direct**        | ⭐⭐ Moyen | 1 min  | ✅ Corrige tous les médicaments |
| **Baisser seuil**     | ⭐ Facile  | 2 min  | ⚠️ Moins strict                 |

---

## 🎯 Ma recommandation

### Pour le développement (maintenant) :

**Utilisez le script PowerShell** (`activer_tous_medicaments.ps1`) :

1. Modifiez votre mot de passe dans le fichier
2. Double-cliquez dessus
3. C'est tout ! ✅

### Pour la production (plus tard) :

- Gardez `confidence-threshold: 0.7` (sécurité)
- Importez régulièrement la BDPM
- Vérifiez manuellement les médicaments inactifs

---

## 🧪 Test après correction

1. **Exécutez** une des solutions ci-dessus
2. **Rechargez l'application mobile** (secouez + Reload)
3. **Scannez à nouveau** le même médicament : `03400922385624`
4. **Résultat attendu** : **✅ AUTHENTIC** (au lieu de SUSPICIOUS)

---

**Quelle méthode voulez-vous utiliser ?**

1. Script PowerShell (je vous guide pour modifier le mot de passe)
2. Swagger avec auth (je vous donne des instructions détaillées)
3. Autre méthode ?

Dites-moi et on corrige ça tout de suite ! 🚀


