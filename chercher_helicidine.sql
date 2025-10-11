-- Chercher l'HELICIDINE dans la base de données

-- 1. Recherche par GTIN exact
SELECT id, gtin, cip13, cis, name, is_active 
FROM medications 
WHERE gtin = '03400922385624';

-- 2. Recherche par CIP13
SELECT id, gtin, cip13, cis, name, is_active 
FROM medications 
WHERE cip13 = '3400922385624';

-- 3. Recherche par CIS (Code Identifiant Spécialité)
SELECT id, gtin, cip13, cis, name, is_active 
FROM medications 
WHERE cis = 66956700;

-- 4. Recherche par nom (toutes les variantes d'HELICIDINE)
SELECT id, gtin, cip13, cis, name, is_active, pharmaceutical_form
FROM medications 
WHERE LOWER(name) LIKE '%helicidine%'
ORDER BY name;

-- 5. Compter le nombre total de médicaments
SELECT COUNT(*) as "Total médicaments" FROM medications;

-- 6. Compter les médicaments actifs vs inactifs
SELECT 
    is_active,
    COUNT(*) as nombre
FROM medications
GROUP BY is_active;

