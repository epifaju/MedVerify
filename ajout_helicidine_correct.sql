-- Ajout du médicament Hélicidine à la base de données
-- GTIN: 03400922385624

-- 1. Insérer le médicament principal
INSERT INTO medications (
    gtin,
    name,
    generic_name,
    manufacturer,
    manufacturer_country,
    dosage,
    pharmaceutical_form,
    registration_number,
    posology,
    is_active,
    requires_prescription,
    is_essential,
    created_at,
    updated_at
) VALUES (
    '03400922385624',
    'Hélicidine Toux Sèche & Toux d''irritation',
    'Hélicidine',
    'SANOFI AVENTIS FRANCE',
    'France',
    '100 mg/15 ml',
    'Sirop',
    'CIS-69485624',
    '{"adult": "15 ml, trois fois par jour", "child": "5 à 10 ml, trois fois par jour (enfants de 5 à 15 ans, selon le poids)", "maxDailyDose": 45, "unit": "ml", "frequency": "3 fois par jour", "specialInstructions": "Agiter le flacon avant emploi. Ne pas dépasser 5 jours de traitement sans avis médical. Utiliser le gobelet doseur fourni."}'::jsonb,
    true,
    false,
    false,
    NOW(),
    NOW()
);

-- 2. Insérer les indications
INSERT INTO medication_indications (medication_id, indication)
SELECT id, unnest(ARRAY[
    'Traitement symptomatique des toux sèches',
    'Toux d''irritation des voies respiratoires supérieures',
    'Toux non productives'
]) FROM medications WHERE gtin = '03400922385624';

-- 3. Insérer les effets secondaires
INSERT INTO medication_side_effects (medication_id, side_effect)
SELECT id, unnest(ARRAY[
    'Somnolence (rare)',
    'Réactions allergiques possibles (urticaire, œdème)',
    'Troubles digestifs (nausées, vomissements)',
    'Diarrhée légère'
]) FROM medications WHERE gtin = '03400922385624';

-- 4. Insérer les contre-indications
INSERT INTO medication_contraindications (medication_id, contraindication)
SELECT id, unnest(ARRAY[
    'Allergie à l''hélicidine ou à l''un des excipients',
    'Allergie aux protéines d''escargot',
    'Toux grasse productive (risque d''encombrement bronchique)',
    'Utilisation déconseillée pendant la grossesse et l''allaitement sans avis médical'
]) FROM medications WHERE gtin = '03400922385624';

-- Vérifier l'insertion
SELECT 
    m.gtin, 
    m.name, 
    m.manufacturer,
    m.dosage,
    (SELECT COUNT(*) FROM medication_indications WHERE medication_id = m.id) as nb_indications,
    (SELECT COUNT(*) FROM medication_side_effects WHERE medication_id = m.id) as nb_effects,
    (SELECT COUNT(*) FROM medication_contraindications WHERE medication_id = m.id) as nb_contraindications
FROM medications m 
WHERE m.gtin = '03400922385624';







