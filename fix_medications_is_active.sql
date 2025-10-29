-- Script pour corriger le champ isActive des médicaments
-- Par défaut, tous les médicaments importés depuis la BDPM devraient être actifs

-- 1. Afficher les médicaments actuellement inactifs
SELECT gtin, cip13, name, is_active, updated_at 
FROM medications 
WHERE is_active = false 
LIMIT 20;

-- 2. Mettre à jour TOUS les médicaments pour les marquer comme actifs
-- DÉCOMMENTER LA LIGNE SUIVANTE POUR APPLIQUER LA CORRECTION :
-- UPDATE medications SET is_active = true WHERE is_active = false;

-- 3. Afficher le nombre de médicaments mis à jour
-- SELECT COUNT(*) as nombre_corriges FROM medications WHERE is_active = true;

-- 4. Vérifier un médicament spécifique par GTIN
SELECT gtin, cip13, name, is_active, manufacturer 
FROM medications 
WHERE gtin = '03400922385624' OR cip13 = '3400922385624';

-- 5. CORRECTION SPÉCIFIQUE pour le médicament scanné (HELICIDINE)
-- DÉCOMMENTER LES LIGNES SUIVANTES POUR APPLIQUER :
-- UPDATE medications 
-- SET is_active = true, updated_at = NOW() 
-- WHERE gtin = '03400922385624' OR cip13 = '3400922385624';




