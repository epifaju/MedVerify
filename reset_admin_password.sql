-- Réinitialiser le mot de passe admin à "Admin123!"
-- Hash BCrypt de "Admin123!" : $2a$10$rO0pXZ5qR8qX.vF6hB8uQeGkqR0xBKqYhQxN5qRkYhB8uQeGkqR0x

UPDATE users
SET password = '$2a$10$rO0pXZ5qR8qX.vF6hB8uQeGkqR0xBKqYhQxN5qRkYhB8uQeGkqR0x',
    failed_login_attempts = 0,
    locked_until = NULL,
    is_active = true
WHERE email = 'admin@medverify.gw';

-- Vérifier
SELECT email, role, is_active, failed_login_attempts FROM users WHERE email = 'admin@medverify.gw';







