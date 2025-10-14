# 🔧 FIX - Erreur type colonne rating

## ❌ Problème

```
Schema-validation: wrong column type encountered in column [rating] in table [pharmacies];
found [numeric (Types#NUMERIC)], but expecting [float(53) (Types#FLOAT)]
```

**Cause :** Incompatibilité entre le type Java `Double` et le type SQL `DECIMAL(2,1)`

## ✅ Solution appliquée

La migration a été corrigée : `DECIMAL(2,1)` → `DOUBLE PRECISION`

---

## 🚀 Étapes de correction

### 1️⃣ Exécuter le script de nettoyage

```bash
# Se connecter à PostgreSQL
psql -U postgres -d medverify

# Exécuter le script de correction
\i fix_pharmacies_rating_type.sql

# Ou copier-coller directement :
```

```sql
-- Supprimer les tables dans l'ordre
DROP TABLE IF EXISTS pharmacy_services CASCADE;
DROP TABLE IF EXISTS on_duty_schedules CASCADE;
DROP TABLE IF EXISTS pharmacies CASCADE;

-- Supprimer le type enum
DROP TYPE IF EXISTS duty_type CASCADE;

-- Supprimer l'entrée de migration V10
DELETE FROM flyway_schema_history WHERE version = '10';

-- Vérifier
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;
```

### 2️⃣ Relancer le backend

```bash
cd medverify-backend

# Relancer - la migration V10 va se ré-exécuter avec le bon type
mvn spring-boot:run
```

### 3️⃣ Vérifier

```sql
-- Vérifier le type de la colonne rating
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'pharmacies'
AND column_name = 'rating';

-- Devrait afficher : double precision
```

---

## 🎯 Vérification rapide

**Si tout fonctionne, vous devriez voir dans les logs :**

```
✅ Successfully validated 9 migrations
✅ Migrating schema "public" to version "10 - pharmacies system"
✅ Successfully applied 1 migration to schema "public", now at version v10
✅ Started MedVerifyApplication in X.XXX seconds
```

---

## ⚡ One-liner (si psql configuré)

```bash
psql -U postgres -d medverify -c "DROP TABLE IF EXISTS pharmacy_services, on_duty_schedules, pharmacies CASCADE; DROP TYPE IF EXISTS duty_type CASCADE; DELETE FROM flyway_schema_history WHERE version = '10';"
```

Puis relancer : `mvn spring-boot:run`

---

## 📝 Explications techniques

### Pourquoi `DOUBLE PRECISION` au lieu de `DECIMAL` ?

| Type Java    | Type SQL PostgreSQL | Type Hibernate |
| ------------ | ------------------- | -------------- |
| `Double`     | `DOUBLE PRECISION`  | `FLOAT` ✅     |
| `Double`     | `DECIMAL(x,y)`      | `NUMERIC` ❌   |
| `BigDecimal` | `DECIMAL(x,y)`      | `NUMERIC` ✅   |

**Pour un `Double` Java, il faut `DOUBLE PRECISION` en SQL.**

### Alternative : Utiliser BigDecimal

Si vous préférez garder `DECIMAL(2,1)` en SQL, modifiez l'entité :

```java
// Dans Pharmacy.java
@Column(name = "rating", precision = 2, scale = 1)
private BigDecimal rating; // Au lieu de Double
```

Mais ici, `DOUBLE PRECISION` est plus simple et adapté pour une notation 0-5.

---

## ✅ C'est corrigé !

La migration est maintenant correcte. Le backend devrait démarrer sans erreur après avoir nettoyé l'ancienne table.
