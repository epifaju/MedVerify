-- MedVerify Initial Schema Migration
-- Version 1.0 - User Authentication

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable PostGIS for geospatial data
CREATE EXTENSION IF NOT EXISTS postgis;

-- Create ENUM type for user roles
CREATE TYPE user_role AS ENUM ('PATIENT', 'PHARMACIST', 'AUTHORITY', 'ADMIN');

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE
);

-- Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token VARCHAR(512) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP WITH TIME ZONE
);

-- Indexes for users table
CREATE UNIQUE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_is_active ON users(is_active);
CREATE INDEX idx_user_created_at ON users(created_at);

-- Indexes for refresh_tokens table
CREATE UNIQUE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_expires ON refresh_tokens(expires_at);

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update updated_at on users table
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert default admin user (password: Admin@123456)
INSERT INTO users (email, password, role, first_name, last_name, is_verified, is_active)
VALUES (
    'admin@medverify.gw',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyujmu3S0VSi',
    'ADMIN',
    'System',
    'Administrator',
    TRUE,
    TRUE
);

-- Comments for documentation
COMMENT ON TABLE users IS 'Users table containing all application users';
COMMENT ON TABLE refresh_tokens IS 'Refresh tokens for JWT authentication';
COMMENT ON COLUMN users.locked_until IS 'Timestamp until which the account is locked after failed login attempts';
COMMENT ON COLUMN users.failed_login_attempts IS 'Number of consecutive failed login attempts';


