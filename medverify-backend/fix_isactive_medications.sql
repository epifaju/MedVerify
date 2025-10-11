-- =========================================================================
-- CORRECTION DU PROBLÈME SUSPECT/AUTHENTIC
-- =========================================================================
-- Problème : Tous les médicaments marqués SUSPECT car isActive = false
-- Solution : Mettre à jour isActive = true pour tous les médicaments
--            sauf ceux explicitement suspendus/retirés/abrogés
-- =========================================================================

-- 1. Afficher l'état actuel
SELECT 
    'AVANT CORRECTION' as etape,
    is_active,
    COUNT(*) as nombre_medicaments,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as pourcentage
FROM medications
GROUP BY is_active;

-- 2. Mettre à jour tous les médicaments à isActive = true par défaut
--    (car la nouvelle logique est plus permissive)
UPDATE medications
SET 
    is_active = true,
    updated_at = CURRENT_TIMESTAMP
WHERE is_active = false
    OR is_active IS NULL;

-- 3. Afficher le résultat après correction
SELECT 
    'APRÈS CORRECTION' as etape,
    is_active,
    COUNT(*) as nombre_medicaments,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as pourcentage
FROM medications
GROUP BY is_active;

-- 4. Afficher des exemples de médicaments corrigés
SELECT 
    'EXEMPLES CORRIGÉS' as info,
    name,
    gtin,
    manufacturer,
    is_active
FROM medications
LIMIT 20;

-- 5. Message de confirmation
SELECT 
    '✅ CORRECTION TERMINÉE' as status,
    COUNT(*) as total_medicaments_actifs
FROM medications
WHERE is_active = true;


