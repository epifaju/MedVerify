-- Migration V8 : Ajout colonne CIP13 pour correspondance avec GTIN

-- 1. Ajouter colonne CIP13 (Code Identifiant Présentation 13 chiffres)
ALTER TABLE medications 
ADD COLUMN IF NOT EXISTS cip13 VARCHAR(13);

COMMENT ON COLUMN medications.cip13 IS 'Code CIP13 (13 chiffres) - Correspond aux 13 derniers chiffres du GTIN';

-- 2. Créer index pour recherche rapide par CIP13
CREATE INDEX IF NOT EXISTS idx_medications_cip13 ON medications(cip13);

-- 3. Mettre à jour les CIP13 pour les médicaments existants (extraire du GTIN)
UPDATE medications
SET cip13 = CASE 
    WHEN LENGTH(gtin) = 14 THEN SUBSTRING(gtin, 2, 13)
    WHEN LENGTH(gtin) = 13 THEN gtin
    ELSE NULL
END
WHERE cip13 IS NULL;

-- 4. Ajouter colonne CIS (Code Identifiant Spécialité) si pas déjà présent
ALTER TABLE medications 
ADD COLUMN IF NOT EXISTS cis BIGINT;

COMMENT ON COLUMN medications.cis IS 'Code CIS (Code Identifiant Spécialité) - Identifiant unique BDPM';

-- 5. Créer index pour CIS
CREATE INDEX IF NOT EXISTS idx_medications_cis ON medications(cis);

-- 6. Vue pour correspondance GTIN ↔ CIP13 ↔ CIS
CREATE OR REPLACE VIEW medication_identifiers AS
SELECT 
    id,
    gtin,
    cip13,
    cis,
    name,
    manufacturer,
    data_source
FROM medications
WHERE gtin IS NOT NULL OR cip13 IS NOT NULL OR cis IS NOT NULL;

COMMENT ON VIEW medication_identifiers IS 'Vue de correspondance entre les différents identifiants (GTIN, CIP13, CIS)';





