-- MedVerify Composite Indexes Migration
-- Version 12.0 - Performance Optimization
-- 
-- Ajoute des index composites pour optimiser les requêtes fréquentes
-- sur scan_history et medication_batches

-- Index composite pour requêtes fréquentes sur scan_history
-- Utilisé pour : Statistiques par GTIN, statut et date
-- Améliore les requêtes du dashboard et analytics
CREATE INDEX IF NOT EXISTS idx_scan_gtin_status_date 
ON scan_history(gtin, status, scanned_at DESC);

-- Index pour détection rapide de numéros de série dupliqués
-- Utilisé pour : Vérification de contrefaçons (serial_number + gtin)
-- WHERE clause pour optimiser l'index (seulement si serial_number existe)
CREATE INDEX IF NOT EXISTS idx_scan_serial_gtin 
ON scan_history(serial_number, gtin) 
WHERE serial_number IS NOT NULL;

-- Index composite pour recherches de lots rappelés
-- Utilisé pour : Détection rapide de lots rappelés par médicament
-- WHERE clause pour réduire la taille de l'index (seulement lots rappelés)
CREATE INDEX IF NOT EXISTS idx_batch_medication_recalled 
ON medication_batches(medication_id, is_recalled, expiry_date DESC) 
WHERE is_recalled = TRUE;

-- Index composite pour requêtes de statistiques utilisateur
-- Utilisé pour : Historique des scans par utilisateur avec tri par date
CREATE INDEX IF NOT EXISTS idx_scan_user_date 
ON scan_history(user_id, scanned_at DESC, status);

-- Index partiel pour scans suspects (pour alertes)
-- Utilisé pour : Dashboard autorités - filtrer rapidement les scans suspects
CREATE INDEX IF NOT EXISTS idx_scan_suspects 
ON scan_history(status, scanned_at DESC) 
WHERE status = 'SUSPICIOUS';

-- Index composite pour recherche de médicaments actifs essentiels
-- Utilisé pour : Liste des médicaments essentiels actifs
CREATE INDEX IF NOT EXISTS idx_medication_essential_active 
ON medications(is_essential, is_active, name) 
WHERE is_essential = TRUE AND is_active = TRUE;

-- Commentaires pour documentation
COMMENT ON INDEX idx_scan_gtin_status_date IS 'Optimise les requêtes de statistiques par GTIN, statut et date';
COMMENT ON INDEX idx_scan_serial_gtin IS 'Optimise la détection de numéros de série dupliqués';
COMMENT ON INDEX idx_batch_medication_recalled IS 'Optimise la recherche de lots rappelés par médicament';
COMMENT ON INDEX idx_scan_user_date IS 'Optimise l''historique des scans par utilisateur';
COMMENT ON INDEX idx_scan_suspects IS 'Optimise les requêtes de scans suspects pour le dashboard';
COMMENT ON INDEX idx_medication_essential_active IS 'Optimise la recherche de médicaments essentiels actifs';

