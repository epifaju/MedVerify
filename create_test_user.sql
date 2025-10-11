-- Créer un utilisateur de test pour l'application mobile
-- Mot de passe : test123456
-- Hash BCrypt généré pour "test123456"

INSERT INTO users (id, email, password, first_name, last_name, role, language, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'test@medverify.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMqeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- test123456
    'Test',
    'User',
    'PATIENT',
    'fr',
    true,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO UPDATE
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMqeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    updated_at = NOW();

-- Afficher le résultat
SELECT email, first_name, last_name, role FROM users WHERE email = 'test@medverify.com';

