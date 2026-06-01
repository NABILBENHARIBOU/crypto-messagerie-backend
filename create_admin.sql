-- Script SQL pour créer un compte admin
-- Exécutez ce script dans MySQL après vous être connecté à la base de données messagerie_db

-- Vérifier que la base de données existe
USE messagerie_db;

-- Insérer l'utilisateur admin
INSERT INTO user (first_name, last_name, email, password_hash, phone, role, blocked, two_factor_enabled, created_at, updated_at) 
VALUES ('Admin', 'User', 'admin@messagerie.com', 'pTYlflcDqa1+OpNNc3Y+DYokJTOg1tauV8LJNcmdre8Tpix1WX0pmLTPPT1/f0TZ', '', 'ADMIN', false, false, '2026-05-31 16:40:27', '2026-05-31 16:40:27');

-- Vérifier que l'utilisateur admin a été créé
SELECT id, first_name, last_name, email, role FROM user WHERE email = 'admin@messagerie.com';

-- Afficher tous les utilisateurs pour vérification
SELECT id, first_name, last_name, email, role FROM user;
