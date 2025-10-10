# 🔧 Fix Flyway - Migration V3 Conflit

## Problème

Flyway détecte 2 migrations V3 différentes :

- L'ancienne V3 déjà appliquée en base
- Notre nouvelle V4 (renommée de V3)

## ✅ Solution Simple

### Option 1 : Supprimer l'Ancienne Migration V3 en Base (Recommandé)

Ouvrez **pgAdmin** et exécutez :

```sql
-- Voir toutes les migrations
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Supprimer la migration V3 problématique
DELETE FROM flyway_schema_history WHERE version = '3';

-- Vérifier
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

Vous devriez voir seulement V1 et V2.

### Option 2 : Via psql (Terminal)

```powershell
psql -U postgres -d medverify -c "DELETE FROM flyway_schema_history WHERE version = '3';"
```

---

## 🚀 Après le Fix

1. **Relancez le backend** :

```powershell
cd medverify-backend
mvn clean spring-boot:run
```

2. **Vérifiez les logs** :

```
Successfully validated 3 migrations
Current version of schema "public": 4
Schema "public" is up to date. No migration necessary.
```

3. **Testez** : http://localhost:8080/actuator/health

---

## ⚡ Exécution Rapide

**Copiez-collez dans pgAdmin Query Tool** :

```sql
DELETE FROM flyway_schema_history WHERE version = '3';
```

Puis relancez : `mvn spring-boot:run`

C'est tout ! 🎉
