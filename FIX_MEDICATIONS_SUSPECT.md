# 🔧 Corriger les médicaments marqués SUSPECT

## 🎯 Problème identifié

**Pourquoi les médicaments sont SUSPECT ?**

L'algorithme de vérification utilise un **système de score de confiance** :

```
Score initial : 1.0 (100%)

Pénalités :
- isActive = false       → -0.5  (médicament non commercialisé)
- Série dupliquée ≥5 fois → -0.6  (possiblement contrefait)
- Périmé                 → -0.3  (date d'expiration dépassée)
- Lot rappelé            → -0.8  (lot retiré du marché)

Décision :
✅ Score ≥ 0.7 → AUTHENTIC
⚠️ Score < 0.7 → SUSPICIOUS
```

**Cause la plus fréquente** : `isActive = false`
- Score = 1.0 - 0.5 = **0.5**
- 0.5 < 0.7 → **SUSPICIOUS**

---

## ✅ Solution 1 : Corriger via SQL (RAPIDE)

### Étape 1 : Vérifier le statut du médicament

Ouvrez **pgAdmin** ou **DBeaver** et connectez-vous à la base `medverify`.

Exécutez :
```sql
SELECT gtin, cip13, name, is_active, manufacturer 
FROM medications 
WHERE gtin = '03400922385624' OR cip13 = '3400922385624';
```

### Étape 2 : Si `is_active = false`, corrigez-le

```sql
UPDATE medications 
SET is_active = true, updated_at = NOW() 
WHERE gtin = '03400922385624' OR cip13 = '3400922385624';
```

### Étape 3 : Corriger TOUS les médicaments inactifs (si nécessaire)

```sql
-- Voir combien de médicaments sont inactifs
SELECT COUNT(*) FROM medications WHERE is_active = false;

-- Corriger TOUS les médicaments
UPDATE medications SET is_active = true WHERE is_active = false;
```

---

## ✅ Solution 2 : Modifier le seuil de confiance (ALTERNATIVE)

Si vous voulez que des médicaments avec `isActive = false` soient quand même considérés comme AUTHENTIC, vous pouvez baisser le seuil de confiance.

### Modifier `application.yml`

```yaml
# medverify-backend/src/main/resources/application.yml
app:
  verification:
    confidence-threshold: 0.5  # Au lieu de 0.7 (par défaut)
```

**Avec ce changement** :
- Score 0.5 → **AUTHENTIC** ✅ (au lieu de SUSPICIOUS)
- Mais attention : des médicaments potentiellement problématiques pourraient être marqués comme authentiques

---

## ✅ Solution 3 : Ajuster les pénalités (AVANCÉ)

Modifiez les pénalités dans le code source :

### Fichier : `MedicationVerificationService.java`

Ligne 274-282 (Règle 1) :
```java
// AVANT
if (!medication.getIsActive()) {
    confidence -= 0.5;  // Pénalité forte
}

// APRÈS (plus permissif)
if (!medication.getIsActive()) {
    confidence -= 0.2;  // Pénalité plus faible
    // Score = 1.0 - 0.2 = 0.8 → AUTHENTIC ✅
}
```

**⚠️ Attention** : Cette modification nécessite de recompiler le backend.

---

## 🔍 Comprendre le statut `isActive`

### Qu'est-ce que `isActive` ?

- `isActive = true` → Médicament **actuellement commercialisé**
- `isActive = false` → Médicament **retiré du marché** ou **plus commercialisé**

### D'où vient cette information ?

Lors de l'import de la **Base BDPM** (Base de Données Publique des Médicaments), le champ `isActive` reflète si le médicament est actuellement autorisé à la vente en France.

---

## 🧪 Cas d'usage réels

### Scénario 1 : Médicament réellement actif mais marqué inactif dans la DB

**Cause** : Erreur d'import ou données BDPM obsolètes

**Solution** : Mettre à jour `isActive = true` manuellement (SQL ci-dessus)

### Scénario 2 : Médicament vraiment retiré du marché

**Cause** : Le médicament a été retiré de la commercialisation

**Action** : Le statut SUSPICIOUS est **CORRECT** ! C'est un signal d'alerte légitime.

---

## 💡 Recommandation

### Pour le développement :

1. **Mettez tous les médicaments à `isActive = true`** dans la base de données :
```sql
UPDATE medications SET is_active = true;
```

2. **OU baissez le seuil** dans `application.yml` :
```yaml
confidence-threshold: 0.5
```

### Pour la production :

1. **Gardez le seuil à 0.7** (sécurité)
2. **Importez régulièrement la BDPM** pour avoir des données à jour
3. **Vérifiez manuellement** les médicaments marqués inactifs

---

## 🚀 Action rapide : Corriger via Swagger

1. **Ouvrez** : `http://192.168.1.16:8080/swagger-ui.html`

2. **Trouvez** l'endpoint `GET /api/v1/medications/search?name=...`

3. **Cherchez** le médicament scanné

4. **Vérifiez** la valeur de `isActive`

5. Si nécessaire, utilisez un outil SQL pour mettre à jour

---

## 📊 Résumé

| Médicament | GTIN scanné | CIP13 | isActive ? | Score | Status |
|-----------|------------|-------|-----------|-------|--------|
| HELICIDINE | 03400922385624 | 3400922385624 | ❓ | ? | ⚠️ SUSPICIOUS |

**Si `isActive = false` → Score 0.5 → SUSPICIOUS**
**Si `isActive = true` → Score 1.0 → AUTHENTIC** ✅

---

**Quelle solution préférez-vous ?**

1. 🔧 Mettre à jour `isActive = true` via SQL
2. ⚙️ Baisser le seuil de confiance à 0.5
3. 🔍 Vérifier d'abord le statut via Swagger

Dites-moi et je vous guide ! 🚀




