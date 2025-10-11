-- Migration V9 : Table pour les codes de vérification email/SMS
-- Permet l'activation des comptes avec code 6 chiffres

CREATE TABLE verification_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code VARCHAR(6) NOT NULL,
    type VARCHAR(20) NOT NULL, -- EMAIL ou SMS
    email VARCHAR(255),
    phone VARCHAR(20),
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    attempts INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 3,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_type CHECK (type IN ('EMAIL', 'SMS')),
    CONSTRAINT chk_code CHECK (code ~ '^[0-9]{6}$') -- Exactement 6 chiffres
);

-- Index pour recherche rapide par user_id et type
CREATE INDEX idx_verification_user_type ON verification_codes(user_id, type);

-- Index pour nettoyage des codes expirés
CREATE INDEX idx_verification_expires ON verification_codes(expires_at);

-- Index pour recherche par code (vérification)
CREATE INDEX idx_verification_code ON verification_codes(code);

-- Commentaires
COMMENT ON TABLE verification_codes IS 'Codes de vérification pour activation des comptes (email/SMS)';
COMMENT ON COLUMN verification_codes.code IS 'Code à 6 chiffres envoyé par email ou SMS';
COMMENT ON COLUMN verification_codes.type IS 'Type de vérification : EMAIL ou SMS';
COMMENT ON COLUMN verification_codes.expires_at IS 'Date d''expiration du code (15 minutes après génération)';
COMMENT ON COLUMN verification_codes.verified_at IS 'Date de vérification (NULL si pas encore vérifié)';
COMMENT ON COLUMN verification_codes.attempts IS 'Nombre de tentatives de vérification';
COMMENT ON COLUMN verification_codes.max_attempts IS 'Nombre maximum de tentatives (3 par défaut)';

