-- Ajouter manuellement l'HELICIDINE PHOLCODINE dans la base

INSERT INTO medications (
    id,
    gtin,
    cip13,
    cis,
    name,
    generic_name,
    manufacturer,
    manufacturer_country,
    dosage,
    pharmaceutical_form,
    registration_number,
    indications,
    side_effects,
    contraindications,
    is_active,
    requires_prescription,
    is_essential,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    '03400922385624',                           -- GTIN
    '3400922385624',                            -- CIP13
    66956700,                                   -- CIS (Code Identifiant Spécialité)
    'HELICIDINE PHOLCODINE 5 mg/5 ml, sirop',  -- Nom
    'PHOLCODINE',                               -- Nom générique
    'LABORATOIRE CHAUVIN',                      -- Fabricant
    'France',                                   -- Pays
    '5 mg/5 ml',                                -- Dosage
    'Sirop',                                    -- Forme pharmaceutique
    'CIS-66956700',                             -- Numéro d'enregistrement
    ARRAY['Toux sèche', 'Antitussif'],         -- Indications
    ARRAY['Somnolence', 'Constipation'],       -- Effets secondaires
    ARRAY['Allergie à la pholcodine', 'Insuffisance respiratoire'], -- Contre-indications
    false,                                      -- is_active (RETIRÉ DU MARCHÉ EN 2022)
    false,                                      -- Prescription
    false,                                      -- Essentiel
    NOW(),
    NOW()
);

-- Vérifier l'ajout
SELECT gtin, cip13, name, is_active, manufacturer 
FROM medications 
WHERE gtin = '03400922385624';

-- ✅ HELICIDINE ajouté !
-- Note : is_active = false car médicament VRAIMENT retiré du marché en 2022
-- Le statut SUSPECT sera donc correct et justifié



