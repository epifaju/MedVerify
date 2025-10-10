-- Fix: Convertir TOUS les types ENUM vers VARCHAR
-- pour compatibilité totale avec Hibernate @Enumerated(EnumType.STRING)

-- 1. Convertir report.status (report_status → VARCHAR)
ALTER TABLE reports 
ALTER COLUMN status TYPE VARCHAR(20) USING status::VARCHAR;

-- 2. Convertir report.report_type (report_type → VARCHAR)  
ALTER TABLE reports 
ALTER COLUMN report_type TYPE VARCHAR(50) USING report_type::VARCHAR;

-- 3. Supprimer les anciens types ENUM
DROP TYPE IF EXISTS report_status CASCADE;
DROP TYPE IF EXISTS report_type CASCADE;

-- 4. Ajouter des commentaires
COMMENT ON COLUMN reports.status IS 'Report status: SUBMITTED, UNDER_REVIEW, CONFIRMED, REJECTED, CLOSED';
COMMENT ON COLUMN reports.report_type IS 'Report type: COUNTERFEIT, QUALITY_ISSUE, ADVERSE_REACTION, PACKAGING_DAMAGE, SUSPICIOUS_SOURCE, OTHER';

