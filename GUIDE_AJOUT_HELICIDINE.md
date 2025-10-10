# 💊 Guide Rapide - Ajouter Hélicidine à la base locale

## Méthode rapide (3 minutes)

### Option 1 : Via terminal PowerShell

**1. Ouvrir un NOUVEAU PowerShell** (pas celui d'Expo)

**2. Copier-coller ces commandes une par une :**

```powershell
cd C:\Users\epifa\cursor-workspace\MedVerify
```

```powershell
$env:PGPASSWORD='Malagueta7'
```

```powershell
psql -U postgres -d medverify -f ajout_helicidine_correct.sql
```

**3. Vérifier l'ajout :**

```powershell
psql -U postgres -d medverify -c "SELECT gtin, name FROM medications WHERE gtin = '03400922385624';"
```

**Résultat attendu :**

```
      gtin      |                  name
----------------+----------------------------------------
 03400922385624 | Hélicidine Toux Sèche & Toux d'irritation
```

---

### Option 2 : Via pgAdmin (interface graphique)

**1. Ouvrir pgAdmin**

**2. Se connecter à la base `medverify`**

**3. Ouvrir Query Tool** (Outils > Query Tool)

**4. Ouvrir le fichier** `ajout_helicidine_correct.sql`

**5. Cliquer sur Execute (F5)**

✅ Hélicidine sera ajouté !

---

### Option 3 : Requête SQL directe (copier-coller)

Ouvrez pgAdmin ou DBeaver et exécutez :

```sql
-- Ajouter Hélicidine
INSERT INTO medications (
    gtin, name, generic_name, manufacturer, manufacturer_country,
    dosage, pharmaceutical_form, posology, is_active
) VALUES (
    '03400922385624',
    'Hélicidine Toux Sèche & Toux d''irritation',
    'Hélicidine',
    'SANOFI AVENTIS FRANCE',
    'France',
    '100 mg/15 ml',
    'Sirop',
    '{"adult": "15 ml, 3 fois/jour", "child": "5-10 ml, 3 fois/jour", "maxDailyDose": 45, "unit": "ml", "frequency": "3 fois/jour"}'::jsonb,
    true
);

-- Ajouter les indications
INSERT INTO medication_indications (medication_id, indication)
SELECT id, unnest(ARRAY[
    'Traitement des toux sèches',
    'Toux d''irritation'
]) FROM medications WHERE gtin = '03400922385624';

-- Vérifier
SELECT gtin, name FROM medications WHERE gtin = '03400922385624';
```

---

## 🧪 Test après ajout

**1. Rescannez le code Hélicidine** avec votre app mobile

**2. Résultat attendu :**

```
✅ Status: AUTHENTIC
📦 Source: Cache local (ou DB_LOCAL)
💊 Nom: Hélicidine Toux Sèche & Toux d'irritation
🏭 Fabricant: SANOFI AVENTIS FRANCE
```

---

## 🚀 Si ça ne marche toujours pas

Vérifiez les logs backend :

```bash
cd medverify-backend
tail -f logs/medverify.log | Select-String "Hélicidine","03400922385624"
```

Ou redémarrez le backend :

```bash
mvn spring-boot:run
```

---

**Durée estimée : 3 minutes** ⏱️

