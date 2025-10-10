-- Fix: Convertir la colonne status de ENUM vers VARCHAR
-- pour compatibilité avec @Enumerated(EnumType.STRING) de Hibernate

-- Modifier la colonne status dans scan_history
ALTER TABLE scan_history 
ALTER COLUMN status TYPE VARCHAR(20) USING status::VARCHAR;

-- Supprimer l'ancien type ENUM (s'il n'est plus utilisé)
DROP TYPE IF EXISTS verification_status CASCADE;

-- Ajouter un commentaire
COMMENT ON COLUMN scan_history.status IS 'Verification status: AUTHENTIC, SUSPICIOUS, UNKNOWN';

