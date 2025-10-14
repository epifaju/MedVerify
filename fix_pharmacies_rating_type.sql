-- Script de correction pour le type de colonne rating dans pharmacies

-- Supprimer les tables liées (dans l'ordre à cause des contraintes)
DROP TABLE IF EXISTS pharmacy_services CASCADE;
DROP TABLE IF EXISTS on_duty_schedules CASCADE;
DROP TABLE IF EXISTS pharmacies CASCADE;

-- Supprimer le type enum
DROP TYPE IF EXISTS duty_type CASCADE;

-- Supprimer l'entrée de la migration V10 dans flyway_schema_history
DELETE FROM flyway_schema_history WHERE version = '10';

-- Vérification
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;

