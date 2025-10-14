-- Migration V10: Système de pharmacies avec géolocalisation PostGIS

-- Extension PostGIS (si pas déjà activée)
CREATE EXTENSION IF NOT EXISTS postgis;

-- Type enum pour types de garde
DO $$ BEGIN
    CREATE TYPE duty_type AS ENUM ('WEEKEND', 'HOLIDAY', 'NIGHT', 'SPECIAL');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Table Pharmacies
CREATE TABLE IF NOT EXISTS pharmacies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    license_number VARCHAR(20) UNIQUE,
    phone_number VARCHAR(20),
    email VARCHAR(255),
    
    -- Adresse
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    region VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    country VARCHAR(100) NOT NULL DEFAULT 'Guinée-Bissau',
    
    -- Géolocalisation PostGIS
    location geography(Point, 4326) NOT NULL,
    
    -- Horaires (JSONB)
    opening_hours JSONB,
    
    -- Type
    is_24h BOOLEAN DEFAULT FALSE,
    is_night_pharmacy BOOLEAN DEFAULT FALSE,
    accepts_emergencies BOOLEAN DEFAULT TRUE,
    
    -- Responsable
    owner_id UUID REFERENCES users(id),
    
    -- Statut
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Infos complémentaires
    photo_url VARCHAR(500),
    description TEXT,
    website_url VARCHAR(500),
    
    -- Notation
    rating DOUBLE PRECISION CHECK (rating >= 0 AND rating <= 5),
    total_reviews INTEGER DEFAULT 0,
    
    -- Métadonnées
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table Services Pharmacie
CREATE TABLE IF NOT EXISTS pharmacy_services (
    pharmacy_id UUID REFERENCES pharmacies(id) ON DELETE CASCADE,
    service VARCHAR(50) NOT NULL,
    PRIMARY KEY (pharmacy_id, service)
);

-- Table Gardes
CREATE TABLE IF NOT EXISTS on_duty_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pharmacy_id UUID NOT NULL REFERENCES pharmacies(id) ON DELETE CASCADE,
    
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    
    duty_type duty_type NOT NULL,
    
    start_time TIME,
    end_time TIME,
    
    region VARCHAR(100) NOT NULL,
    district VARCHAR(100),
    
    notes TEXT,
    
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    
    CONSTRAINT check_dates CHECK (end_date >= start_date)
);

-- Index pour performances
CREATE INDEX IF NOT EXISTS idx_pharmacies_city ON pharmacies(city);
CREATE INDEX IF NOT EXISTS idx_pharmacies_region ON pharmacies(region);
CREATE INDEX IF NOT EXISTS idx_pharmacies_active ON pharmacies(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_pharmacies_24h ON pharmacies(is_24h) WHERE is_24h = TRUE;
CREATE INDEX IF NOT EXISTS idx_pharmacies_night ON pharmacies(is_night_pharmacy) WHERE is_night_pharmacy = TRUE;

-- Index géospatiaux (GIST pour PostGIS)
CREATE INDEX IF NOT EXISTS idx_pharmacies_location ON pharmacies USING GIST(location);

CREATE INDEX IF NOT EXISTS idx_on_duty_date_range ON on_duty_schedules(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_on_duty_region ON on_duty_schedules(region);
CREATE INDEX IF NOT EXISTS idx_on_duty_active ON on_duty_schedules(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_on_duty_pharmacy ON on_duty_schedules(pharmacy_id);

-- Trigger pour updated_at (réutiliser la fonction existante)
CREATE TRIGGER update_pharmacies_updated_at 
BEFORE UPDATE ON pharmacies
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Données de test (Pharmacies Bissau)
INSERT INTO pharmacies (name, address, city, region, country, location, phone_number, is_24h, is_active, is_verified)
VALUES 
(
    'Pharmacie Centrale de Bissau',
    'Avenue Amilcar Cabral, Centre-ville',
    'Bissau',
    'Bissau',
    'Guinée-Bissau',
    ST_SetSRID(ST_MakePoint(-15.5984, 11.8636), 4326)::geography,
    '+245 955 123 456',
    TRUE,
    TRUE,
    TRUE
),
(
    'Pharmacie du Port',
    'Rua do Porto, Quartier Bairro Militar',
    'Bissau',
    'Bissau',
    'Guinée-Bissau',
    ST_SetSRID(ST_MakePoint(-15.5970, 11.8650), 4326)::geography,
    '+245 955 234 567',
    FALSE,
    TRUE,
    TRUE
),
(
    'Pharmacie de Nuit Bissau',
    'Avenue 14 de Novembro',
    'Bissau',
    'Bissau',
    'Guinée-Bissau',
    ST_SetSRID(ST_MakePoint(-15.5995, 11.8620), 4326)::geography,
    '+245 955 345 678',
    FALSE,
    TRUE,
    TRUE
)
ON CONFLICT (license_number) DO NOTHING;

-- Horaires Pharmacie du Port (exemple JSON)
UPDATE pharmacies 
SET opening_hours = '{
    "monday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00"},
    "tuesday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00"},
    "wednesday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00"},
    "thursday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00"},
    "friday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00"},
    "saturday": {"closed": false, "morningOpen": "09:00", "morningClose": "13:00"},
    "sunday": {"closed": true}
}'::jsonb
WHERE name = 'Pharmacie du Port';

-- Horaires Pharmacie de Nuit
UPDATE pharmacies
SET opening_hours = '{
    "monday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00", "eveningOpen": "20:00", "eveningClose": "23:59"},
    "tuesday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00", "eveningOpen": "20:00", "eveningClose": "23:59"},
    "wednesday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00", "eveningOpen": "20:00", "eveningClose": "23:59"},
    "thursday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00", "eveningOpen": "20:00", "eveningClose": "23:59"},
    "friday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00", "eveningOpen": "20:00", "eveningClose": "23:59"},
    "saturday": {"closed": false, "morningOpen": "08:00", "morningClose": "13:00", "afternoonOpen": "15:00", "afternoonClose": "19:00", "eveningOpen": "20:00", "eveningClose": "23:59"},
    "sunday": {"closed": false, "morningOpen": "09:00", "morningClose": "13:00", "eveningOpen": "20:00", "eveningClose": "23:59"}
}'::jsonb,
is_night_pharmacy = TRUE
WHERE name = 'Pharmacie de Nuit Bissau';

-- Services
INSERT INTO pharmacy_services (pharmacy_id, service)
SELECT id, service FROM pharmacies, 
    (VALUES ('DELIVERY'), ('HOME_VISIT'), ('COVID_TEST')) AS services(service)
WHERE name IN ('Pharmacie Centrale de Bissau', 'Pharmacie de Nuit Bissau')
ON CONFLICT DO NOTHING;

-- Garde weekend en cours (exemple)
INSERT INTO on_duty_schedules (pharmacy_id, start_date, end_date, duty_type, region, created_by)
SELECT 
    p.id,
    CURRENT_DATE + (6 - EXTRACT(DOW FROM CURRENT_DATE))::integer, -- Prochain samedi
    CURRENT_DATE + (8 - EXTRACT(DOW FROM CURRENT_DATE))::integer, -- Lundi suivant
    'WEEKEND'::duty_type,
    'Bissau',
    (SELECT id FROM users WHERE role = 'AUTHORITY' LIMIT 1)
FROM pharmacies p
WHERE p.name = 'Pharmacie du Port'
ON CONFLICT DO NOTHING;

-- Commentaires
COMMENT ON TABLE pharmacies IS 'Table des pharmacies avec géolocalisation PostGIS';
COMMENT ON TABLE on_duty_schedules IS 'Plannings de garde des pharmacies';
COMMENT ON COLUMN pharmacies.location IS 'Localisation géographique (Point PostGIS en WGS84)';
COMMENT ON COLUMN pharmacies.opening_hours IS 'Horaires d''ouverture hebdomadaires au format JSON';

