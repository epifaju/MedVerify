-- Migration V7 : Optimisation GTIN et ajout colonnes de tracking pour API externe

-- 1. Améliorer les performances de recherche GTIN avec index normalisé
CREATE INDEX IF NOT EXISTS idx_medications_gtin_normalized 
ON medications(LPAD(gtin, 14, '0'));

-- 2. Ajouter colonne pour tracer la source de données
ALTER TABLE medications 
ADD COLUMN IF NOT EXISTS data_source VARCHAR(50) DEFAULT 'MANUAL';

COMMENT ON COLUMN medications.data_source IS 'Source de données : MANUAL, API_MEDICAMENTS_FR, BDPM, CACHE_LOCAL';

-- 3. Ajouter colonne last_api_sync pour tracking
ALTER TABLE medications 
ADD COLUMN IF NOT EXISTS last_api_sync TIMESTAMP;

COMMENT ON COLUMN medications.last_api_sync IS 'Dernière synchronisation avec une API externe';

-- 4. Mettre à jour les médicaments existants avec source MANUAL
UPDATE medications
SET data_source = 'MANUAL'
WHERE data_source IS NULL;

-- 5. Créer vue pour médicaments avec cache expiré (> 30 jours)
CREATE OR REPLACE VIEW expired_cache_medications AS
SELECT 
    id, 
    gtin, 
    name, 
    data_source,
    updated_at,
    last_api_sync,
    EXTRACT(EPOCH FROM (NOW() - updated_at))/86400 as days_since_update
FROM medications
WHERE updated_at < NOW() - INTERVAL '30 days'
AND data_source IN ('API_MEDICAMENTS_FR', 'BDPM')
ORDER BY updated_at ASC;

COMMENT ON VIEW expired_cache_medications IS 'Vue des médicaments dont le cache est expiré (> 30 jours)';

-- 6. Créer index pour optimiser les requêtes de cache expiré
CREATE INDEX IF NOT EXISTS idx_medications_updated_at 
ON medications(updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_medications_data_source 
ON medications(data_source);

-- 7. Ajouter statistiques utiles pour monitoring
CREATE OR REPLACE VIEW medication_sources_stats AS
SELECT 
    data_source,
    COUNT(*) as count,
    COUNT(CASE WHEN updated_at > NOW() - INTERVAL '30 days' THEN 1 END) as fresh_count,
    COUNT(CASE WHEN updated_at <= NOW() - INTERVAL '30 days' THEN 1 END) as expired_count,
    MIN(updated_at) as oldest_update,
    MAX(updated_at) as newest_update
FROM medications
GROUP BY data_source;

COMMENT ON VIEW medication_sources_stats IS 'Statistiques sur les sources de données des médicaments';





