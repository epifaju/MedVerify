-- Créer un nouvel utilisateur admin de test
-- Email: test@medverify.gw
-- Mot de passe: Test123!
-- Hash généré avec BCrypt strength 12

INSERT INTO users (email, password, role, first_name, last_name, is_verified, is_active, failed_login_attempts, created_at, updated_at)
VALUES (
    'test@medverify.gw',
    '$2a$12$K8qXqJ5qZxQxJ5qZxQxJ5uZxQxJ5qZxQxJ5qZxQxJ5qZxQxJ5qZxQ',
    'ADMIN',
    'Test',
    'Admin',
    TRUE,
    TRUE,
    0,
    NOW(),
    NOW()
)
ON CONFLICT (email) DO UPDATE SET
    password = EXCLUDED.password,
    failed_login_attempts = 0,
    locked_until = NULL,
    is_active = TRUE;

SELECT 'Utilisateur test@medverify.gw créé avec mot de passe: Test123!' as result;

