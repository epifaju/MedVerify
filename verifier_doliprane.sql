-- Vérifier le statut du DOLIPRANE scanné

-- 1. Recherche par GTIN exact
SELECT 
    gtin, 
    cip13, 
    cis,
    name, 
    is_active,
    manufacturer,
    pharmaceutical_form,
    dosage,
    updated_at
FROM medications 
WHERE gtin = '03400935955838';

-- 2. Recherche par CIP13
SELECT 
    gtin, 
    cip13, 
    cis,
    name, 
    is_active,
    manufacturer
FROM medications 
WHERE cip13 = '3400935955838';

-- 3. Recherche par nom (au cas où)
SELECT 
    gtin, 
    cip13, 
    name, 
    is_active,
    dosage,
    pharmaceutical_form
FROM medications 
WHERE LOWER(name) LIKE '%doliprane%'
  AND (gtin LIKE '%935955838%' OR cip13 LIKE '%935955838%')
LIMIT 10;

-- 4. Statistiques globales
SELECT 
    is_active,
    COUNT(*) as nombre,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as pourcentage
FROM medications
GROUP BY is_active;

-- ✅ Ce script vous dira :
-- - Si le médicament existe
-- - Pourquoi il est marqué SUSPECT (is_active = false ?)
-- - Le pourcentage de médicaments actifs vs inactifs dans votre base

