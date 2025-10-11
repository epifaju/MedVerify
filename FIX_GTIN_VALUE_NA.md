# ✅ Fix Valeur GTIN "N/A" dans Top Contrefaçons

## 🐛 Problème Identifié

Dans la section "Top Contrefaçons" du Dashboard, la valeur du GTIN affichait toujours "N/A" au lieu du vrai code GTIN :

```
Paracétamol 500mg
GTIN : N/A  ← Toujours N/A au lieu du vrai GTIN !
12 signalements
```

---

## 🔍 Cause du Problème

### GTIN Hardcodé à "N/A" dans le Backend

**Fichier** : `medverify-backend/src/main/java/com/medverify/service/DashboardService.java`

À la ligne 164, le GTIN était **hardcodé** :

```java
private List<DashboardStatsResponse.TopCounterfeitMedication> getTopCounterfeits(Instant start, Instant end) {
    List<Object[]> suspiciousScans = scanHistoryRepository.findSerialNumberSpikes(start, end, 3);

    return suspiciousScans.stream()
        .limit(10)
        .map(row -> DashboardStatsResponse.TopCounterfeitMedication.builder()
                .medicationName((String) row[2])
                .gtin("N/A")                    // ← HARDCODÉ !
                .reportCount((Long) row[1])
                .lastReported(Instant.now())
                .build())
        .toList();
}
```

### Requête SQL Incomplète

La requête dans `ScanHistoryRepository` ne retournait pas le GTIN :

```java
@Query("SELECT s.serialNumber, COUNT(s), s.medication.name FROM ScanHistory s " +
       // ↑ Pas de GTIN dans le SELECT !
       "WHERE s.scannedAt BETWEEN :start AND :end " +
       "GROUP BY s.serialNumber, s.medication.name " +
       "HAVING COUNT(s) > :threshold " +
       "ORDER BY COUNT(s) DESC")
List<Object[]> findSerialNumberSpikes(...);
```

**Résultat** : Les données retournaient :

- `row[0]` = serialNumber
- `row[1]` = COUNT (nombre de scans)
- `row[2]` = medication.name
- ❌ **Pas de GTIN !**

---

## ✅ Solution Appliquée

### 1. Modification de la Requête SQL

**Fichier** : `ScanHistoryRepository.java`

**Avant** ❌ :

```java
@Query("SELECT s.serialNumber, COUNT(s), s.medication.name FROM ScanHistory s " +
       "WHERE s.scannedAt BETWEEN :start AND :end " +
       "GROUP BY s.serialNumber, s.medication.name " +
       "HAVING COUNT(s) > :threshold " +
       "ORDER BY COUNT(s) DESC")
```

**Après** ✅ :

```java
@Query("SELECT s.serialNumber, COUNT(s), s.medication.name, s.gtin FROM ScanHistory s " +
       //                                                     ↑ Ajout du GTIN
       "WHERE s.scannedAt BETWEEN :start AND :end " +
       "GROUP BY s.serialNumber, s.medication.name, s.gtin " +
       //                                           ↑ Ajout dans GROUP BY
       "HAVING COUNT(s) > :threshold " +
       "ORDER BY COUNT(s) DESC")
```

### 2. Utilisation du GTIN dans DashboardService

**Fichier** : `DashboardService.java`

**Avant** ❌ :

```java
.map(row -> DashboardStatsResponse.TopCounterfeitMedication.builder()
        .medicationName((String) row[2])
        .gtin("N/A")                    // ← Hardcodé
        .reportCount((Long) row[1])
        .lastReported(Instant.now())
        .build())
```

**Après** ✅ :

```java
.map(row -> DashboardStatsResponse.TopCounterfeitMedication.builder()
        .medicationName((String) row[2])
        .gtin((String) row[3])          // ← Récupération depuis la requête
        .reportCount((Long) row[1])
        .lastReported(Instant.now())
        .build())
```

**Mapping des données** :

- `row[0]` = serialNumber
- `row[1]` = COUNT (nombre de scans)
- `row[2]` = medication.name
- `row[3]` = **gtin** ✅ (nouveau)

---

## 🎯 Résultat

### Avant ❌

```
🚫 Top Contrefaçons
┌────────────────────────────────┐
│ Paracétamol 500mg              │
│ GTIN : N/A                     │ ← Toujours N/A !
│                          12    │
│                    11/10/2025  │
└────────────────────────────────┘
```

### Après ✅

```
🚫 Top Contrefaçons
┌────────────────────────────────┐
│ Paracétamol 500mg              │
│ GTIN : 03401234567890          │ ← Vrai GTIN !
│                          12    │
│                    11/10/2025  │
└────────────────────────────────┘
```

---

## 🔧 Fichiers Modifiés

### Backend (2 fichiers)

1. ✅ **`medverify-backend/src/main/java/com/medverify/repository/ScanHistoryRepository.java`**

   - Ajout du GTIN dans le SELECT : `s.gtin`
   - Ajout du GTIN dans le GROUP BY : `s.gtin`

2. ✅ **`medverify-backend/src/main/java/com/medverify/service/DashboardService.java`**
   - Changement : `.gtin("N/A")` → `.gtin((String) row[3])`
   - Ajout d'un commentaire expliquant la récupération

---

## 🧪 Comment Tester

### Prérequis

Il faut avoir des **données de scan** dans la base avec :

- Des médicaments scannés
- Des numéros de série dupliqués (> 3 fois)
- Des GTIN valides dans la table `scan_history`

### Étape 1 : Redémarrer le Backend

```bash
cd medverify-backend
./mvnw spring-boot:run
```

Ou si déjà lancé, arrêter et relancer pour charger les nouvelles modifications.

### Étape 2 : Tester l'API Dashboard

```bash
curl -X GET "http://localhost:8080/api/dashboard/stats?period=30d" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Vérifier la réponse JSON** :

```json
{
  "topCounterfeitMedications": [
    {
      "medicationName": "Paracétamol 500mg",
      "gtin": "03401234567890", // ← Plus de "N/A" !
      "reportCount": 12,
      "lastReported": "2025-10-11T18:30:00Z"
    }
  ]
}
```

### Étape 3 : Tester dans l'Application Mobile

1. **Se reconnecter** (pour JWT valide)
2. **Aller dans Dashboard** (📊)
3. **Scroller vers "Top Contrefaçons"**
4. ✅ **Vérifier** : Le GTIN affiche un vrai code (ex: 03401234567890)

---

## 💡 Détails Techniques

### Structure de la Table scan_history

```sql
CREATE TABLE scan_history (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    medication_id UUID,
    gtin VARCHAR(14) NOT NULL,      -- ← GTIN stocké ici
    serial_number VARCHAR(100),
    batch_number VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    scanned_at TIMESTAMP NOT NULL,
    ...
);
```

### Requête SQL Générée

```sql
SELECT
    s.serial_number,                -- row[0]
    COUNT(s),                       -- row[1]
    s.medication.name,              -- row[2]
    s.gtin                          -- row[3] ← Nouveau !
FROM scan_history s
WHERE s.scanned_at BETWEEN :start AND :end
GROUP BY s.serial_number, s.medication.name, s.gtin
HAVING COUNT(s) > 3
ORDER BY COUNT(s) DESC
```

### Mapping Backend → Frontend

**Backend retourne** :

```java
TopCounterfeitMedication {
    medicationName: "Paracétamol 500mg",
    gtin: "03401234567890",           // ← Depuis row[3]
    reportCount: 12,
    lastReported: "2025-10-11T18:30:00Z"
}
```

**Frontend affiche** :

```typescript
{
  item.medicationName;
} // "Paracétamol 500mg"
{
  t("dashboard_gtin_label");
}
{
  item.gtin;
} // "GTIN : 03401234567890"
{
  item.reportCount;
} // 12
```

---

## 🎉 Résultat Final

🟢 **PROBLÈME RÉSOLU !**

Le GTIN affiche maintenant la **vraie valeur** depuis la base de données :

- ✅ Plus de "N/A" hardcodé
- ✅ Récupération depuis `scan_history.gtin`
- ✅ Affichage correct dans le Dashboard
- ✅ Support multilingue du label (FR/PT/CR)

---

## 🚀 Test Immédiat

### 1. Redémarrer le Backend

```bash
cd medverify-backend
./mvnw spring-boot:run
```

### 2. Tester l'Application

1. **Se reconnecter** (nouveau token)
2. **Dashboard** (📊)
3. **Scroller vers "Top Contrefaçons"**
4. ✅ Vérifier que le GTIN affiche un vrai code

**Exemple attendu** :

```
🚫 Top Contrefaçons
Paracétamol 500mg
GTIN : 03401234567890  ← Plus de N/A !
12 signalements
11/10/2025
```

---

## 📝 Note sur les Données

Si la section "Top Contrefaçons" ne s'affiche pas, c'est qu'il n'y a pas de données avec :

- Numéros de série dupliqués (> 3 scans du même numéro)
- C'est normal pour une base de données vide

Pour tester, il faut :

1. Scanner plusieurs fois le même médicament
2. Avec le même numéro de série
3. Au moins 4 fois (threshold = 3)

**Le GTIN s'affichera correctement dès qu'il y aura des données !** 🎉
