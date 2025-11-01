-- Migration V11: Create audit_logs table for admin action tracking

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255),
    entity_id UUID,
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_audit_username ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp);

-- Comment
COMMENT ON TABLE audit_logs IS 'Table pour journaliser les actions d administration (CREATE, UPDATE, DELETE)';
COMMENT ON COLUMN audit_logs.username IS 'Email ou identifiant de l utilisateur qui a effectué l action';
COMMENT ON COLUMN audit_logs.action IS 'Type d action : PHARMACY_CREATE, PHARMACY_UPDATE, PHARMACY_DELETE, etc.';
COMMENT ON COLUMN audit_logs.entity_type IS 'Type de l entité modifiée : Pharmacy, OnDutySchedule, etc.';
COMMENT ON COLUMN audit_logs.entity_id IS 'ID de l entité modifiée';
COMMENT ON COLUMN audit_logs.old_value IS 'Valeur JSON de l entité avant modification (si UPDATE)';
COMMENT ON COLUMN audit_logs.new_value IS 'Valeur JSON de l entité après modification';





