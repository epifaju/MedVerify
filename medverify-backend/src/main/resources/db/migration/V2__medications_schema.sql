-- MedVerify Medications Schema Migration
-- Version 2.0 - Medications, Batches & Scan History

-- Create ENUM type for verification status
CREATE TYPE verification_status AS ENUM ('AUTHENTIC', 'SUSPICIOUS', 'UNKNOWN');

-- Create medications table
CREATE TABLE medications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gtin VARCHAR(14) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    generic_name VARCHAR(255),
    manufacturer VARCHAR(255),
    manufacturer_country VARCHAR(100),
    dosage VARCHAR(100),
    pharmaceutical_form VARCHAR(100),
    registration_number VARCHAR(50),
    atc_code VARCHAR(20),
    posology JSONB,
    is_essential BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    requires_prescription BOOLEAN NOT NULL DEFAULT FALSE,
    image_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create medication_indications table
CREATE TABLE medication_indications (
    medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    indication VARCHAR(500)
);

-- Create medication_side_effects table
CREATE TABLE medication_side_effects (
    medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    side_effect VARCHAR(500)
);

-- Create medication_contraindications table
CREATE TABLE medication_contraindications (
    medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    contraindication VARCHAR(500)
);

-- Create medication_batches table
CREATE TABLE medication_batches (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    medication_id UUID NOT NULL REFERENCES medications(id) ON DELETE CASCADE,
    batch_number VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    production_date DATE,
    is_recalled BOOLEAN NOT NULL DEFAULT FALSE,
    recall_date DATE,
    recall_reason VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create scan_history table with PostGIS Point
CREATE TABLE scan_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    medication_id UUID REFERENCES medications(id) ON DELETE SET NULL,
    gtin VARCHAR(14) NOT NULL,
    serial_number VARCHAR(100),
    batch_number VARCHAR(50),
    status verification_status NOT NULL,
    confidence DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    alerts JSONB,
    location GEOGRAPHY(Point, 4326),
    device_info JSONB,
    scanned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for medications
CREATE UNIQUE INDEX idx_medication_gtin ON medications(gtin);
CREATE INDEX idx_medication_name ON medications(name);
CREATE INDEX idx_medication_essential ON medications(is_essential);
CREATE INDEX idx_medication_active ON medications(is_active);
CREATE INDEX idx_medication_atc ON medications(atc_code);

-- Indexes for batches
CREATE INDEX idx_batch_number ON medication_batches(batch_number);
CREATE INDEX idx_batch_medication ON medication_batches(medication_id);
CREATE INDEX idx_batch_recalled ON medication_batches(is_recalled);

-- Indexes for scan_history
CREATE INDEX idx_scan_user ON scan_history(user_id);
CREATE INDEX idx_scan_medication ON scan_history(medication_id);
CREATE INDEX idx_scan_gtin ON scan_history(gtin);
CREATE INDEX idx_scan_serial ON scan_history(serial_number);
CREATE INDEX idx_scan_status ON scan_history(status);
CREATE INDEX idx_scan_date ON scan_history(scanned_at);
CREATE INDEX idx_scan_location ON scan_history USING GIST(location);

-- Trigger to update updated_at on medications table
CREATE TRIGGER update_medications_updated_at
    BEFORE UPDATE ON medications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert sample medications (WHO Essential Medicines)
INSERT INTO medications (gtin, name, generic_name, manufacturer, manufacturer_country, dosage, pharmaceutical_form, atc_code, is_essential, is_active, posology)
VALUES
('03401234567890', 'Paracétamol 500mg', 'Paracétamol', 'PharmaCare Labs', 'France', '500mg', 'Comprimé', 'N02BE01', TRUE, TRUE,
 '{"adult": "1-2 comprimés toutes les 4-6h", "child": "Selon poids: 10-15mg/kg", "maxDailyDose": 4, "unit": "g", "frequency": "4-6 fois/jour"}'),

('03401234567891', 'Amoxicilline 500mg', 'Amoxicilline', 'AntibioPharm', 'Belgique', '500mg', 'Gélule', 'J01CA04', TRUE, TRUE,
 '{"adult": "500mg-1g toutes les 8h", "child": "25-50mg/kg/jour en 3 prises", "maxDailyDose": 3, "unit": "g", "frequency": "3 fois/jour"}'),

('03401234567892', 'Ibuprofène 400mg', 'Ibuprofène', 'PharmaGlobal', 'Suisse', '400mg', 'Comprimé', 'M01AE01', TRUE, TRUE,
 '{"adult": "400mg toutes les 6-8h", "child": "5-10mg/kg toutes les 6-8h", "maxDailyDose": 2, "unit": "g", "frequency": "3-4 fois/jour"}'),

('03401234567893', 'Metformine 850mg', 'Metformine', 'DiabetCare', 'Allemagne', '850mg', 'Comprimé', 'A10BA02', TRUE, TRUE,
 '{"adult": "850mg 1-3 fois/jour avec repas", "maxDailyDose": 3, "unit": "g", "frequency": "2-3 fois/jour"}'),

('03401234567894', 'Oméprazole 20mg', 'Oméprazole', 'GastroMed', 'Portugal', '20mg', 'Gélule', 'A02BC01', TRUE, TRUE,
 '{"adult": "20mg 1 fois/jour le matin", "maxDailyDose": 40, "unit": "mg", "frequency": "1-2 fois/jour"}'),

('03401234567895', 'Amlodipine 5mg', 'Amlodipine', 'CardioPharma', 'Espagne', '5mg', 'Comprimé', 'C08CA01', TRUE, TRUE,
 '{"adult": "5-10mg 1 fois/jour", "maxDailyDose": 10, "unit": "mg", "frequency": "1 fois/jour"}'),

('03401234567896', 'Atorvastatine 20mg', 'Atorvastatine', 'LipidControl', 'Italie', '20mg', 'Comprimé', 'C10AA05', TRUE, TRUE,
 '{"adult": "10-20mg 1 fois/jour", "maxDailyDose": 80, "unit": "mg", "frequency": "1 fois/jour"}'),

('03401234567897', 'Salbutamol 100μg', 'Salbutamol', 'RespiroPharma', 'Royaume-Uni', '100μg', 'Inhalateur', 'R03AC02', TRUE, TRUE,
 '{"adult": "1-2 inhalations selon besoin", "child": "1 inhalation selon besoin", "maxDailyDose": 8, "unit": "inhalations", "frequency": "selon besoin"}'),

('03401234567898', 'Losartan 50mg', 'Losartan', 'TensionControl', 'Pays-Bas', '50mg', 'Comprimé', 'C09CA01', TRUE, TRUE,
 '{"adult": "50-100mg 1 fois/jour", "maxDailyDose": 100, "unit": "mg", "frequency": "1 fois/jour"}'),

('03401234567899', 'Ciprofloxacine 500mg', 'Ciprofloxacine', 'InfectioCure', 'Danemark', '500mg', 'Comprimé', 'J01MA02', TRUE, TRUE,
 '{"adult": "500-750mg 2 fois/jour", "maxDailyDose": 1, "unit": "g", "frequency": "2 fois/jour"}');

-- Insert indications
INSERT INTO medication_indications (medication_id, indication)
SELECT id, unnest(ARRAY[
    'Douleur légère à modérée',
    'Fièvre',
    'Céphalées',
    'Douleurs dentaires'
]) FROM medications WHERE gtin = '03401234567890';

INSERT INTO medication_indications (medication_id, indication)
SELECT id, unnest(ARRAY[
    'Infections bactériennes',
    'Pneumonie',
    'Otite',
    'Sinusite',
    'Infections urinaires'
]) FROM medications WHERE gtin = '03401234567891';

INSERT INTO medication_indications (medication_id, indication)
SELECT id, unnest(ARRAY[
    'Douleur inflammatoire',
    'Arthrite',
    'Fièvre',
    'Douleurs menstruelles'
]) FROM medications WHERE gtin = '03401234567892';

-- Insert side effects
INSERT INTO medication_side_effects (medication_id, side_effect)
SELECT id, unnest(ARRAY[
    'Nausées (rare)',
    'Réactions allergiques (très rare)',
    'Toxicité hépatique à forte dose'
]) FROM medications WHERE gtin = '03401234567890';

INSERT INTO medication_side_effects (medication_id, side_effect)
SELECT id, unnest(ARRAY[
    'Diarrhée',
    'Nausées',
    'Éruption cutanée',
    'Réactions allergiques'
]) FROM medications WHERE gtin = '03401234567891';

-- Insert contraindications
INSERT INTO medication_contraindications (medication_id, contraindication)
SELECT id, unnest(ARRAY[
    'Insuffisance hépatique sévère',
    'Allergie au paracétamol',
    'Consommation excessive d''alcool'
]) FROM medications WHERE gtin = '03401234567890';

INSERT INTO medication_contraindications (medication_id, contraindication)
SELECT id, unnest(ARRAY[
    'Allergie aux pénicillines',
    'Mononucléose infectieuse',
    'Antécédents de réactions allergiques graves'
]) FROM medications WHERE gtin = '03401234567891';

-- Insert sample batches
INSERT INTO medication_batches (medication_id, batch_number, expiry_date, production_date)
SELECT id, 'LOT2024A123', '2026-12-31', '2024-01-15'
FROM medications WHERE gtin = '03401234567890';

INSERT INTO medication_batches (medication_id, batch_number, expiry_date, production_date)
SELECT id, 'LOT2024B456', '2026-06-30', '2024-02-10'
FROM medications WHERE gtin = '03401234567891';

-- Insert a recalled batch example
INSERT INTO medication_batches (medication_id, batch_number, expiry_date, production_date, is_recalled, recall_date, recall_reason)
SELECT id, 'LOT2023X999', '2025-12-31', '2023-12-01', TRUE, '2024-03-15', 'Contamination détectée lors du contrôle qualité'
FROM medications WHERE gtin = '03401234567892';

-- Comments for documentation
COMMENT ON TABLE medications IS 'Reference table for medications';
COMMENT ON TABLE medication_batches IS 'Batches of medications with expiry dates and recall information';
COMMENT ON TABLE scan_history IS 'History of all medication scans and verifications';
COMMENT ON COLUMN scan_history.location IS 'Geographic location where the scan was performed (PostGIS Point)';
COMMENT ON COLUMN scan_history.confidence IS 'Confidence score of the verification (0.0 to 1.0)';
COMMENT ON COLUMN scan_history.alerts IS 'JSON array of alerts generated during verification';


