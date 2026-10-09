-- Local demonstration accounts. Password values are BCrypt hashes, never plaintext.
-- Replace or disable this migration in production provisioning.
INSERT INTO utilisateur (id, username, email, password_hash, nom, prenom, role, actif) VALUES
(1, 'admin.demo', 'admin@medicareplus.test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MediCare+', 'Admin', 'ADMIN', TRUE),
(2, 'medecin.demo', 'a.martin@medicareplus.test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Martin', 'Alexandre', 'MEDECIN', TRUE),
(3, 'secretaire.demo', 's.trabelsi@medicareplus.test', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trabelsi', 'Sonia', 'SECRETAIRE', TRUE);

INSERT INTO medecin (id, utilisateur_id, matricule, specialite, telephone) VALUES
(1, 2, 'MED-2025-001', 'Cardiologie', '+33 1 84 80 20 10');

INSERT INTO disponibilite (id, medecin_id, jour_semaine, heure_debut, heure_fin, active) VALUES
(1, 1, 'LUNDI', '08:30:00', '12:30:00', TRUE),
(2, 1, 'MARDI', '14:00:00', '18:00:00', TRUE),
(3, 1, 'JEUDI', '08:30:00', '17:30:00', TRUE);

INSERT INTO patient (id, numero_patient, nom, prenom, date_naissance, sexe, email, telephone, adresse, groupe_sanguin, archive) VALUES
(1, 'PAT-2025-02418', 'Bernard', 'Sophie', '1983-03-18', 'F', 'sophie.bernard@example.test', '+33 6 10 20 30 40', '12 rue des Lilas, Paris', 'O+', FALSE),
(2, 'PAT-2025-02417', 'Dubois', 'Marc', '1969-08-04', 'M', 'marc.dubois@example.test', '+33 6 20 30 40 50', '8 avenue Victor Hugo, Paris', 'A+', FALSE),
(3, 'PAT-2025-02412', 'Moreau', 'Lina', '1994-01-22', 'F', 'lina.moreau@example.test', '+33 6 30 40 50 60', '4 rue Pasteur, Lyon', 'B-', FALSE);

INSERT INTO dossier_medical (id, patient_id, antecedents, allergies, notes) VALUES
(1, 1, 'Hypertension contrôlée', 'Pénicilline', 'Suivi cardiologique trimestriel'),
(2, 2, 'Diabète type 2', NULL, 'Surveillance glycémique'),
(3, 3, 'Intervention orthopédique en 2024', 'Ibuprofène', 'Contrôle post-opératoire');

INSERT INTO medicament (id, code, nom, description, prix, stock, seuil_alerte, actif) VALUES
(1, 'MED-AMOX-500', 'Amoxicilline', 'Gélules 500 mg', 8.50, 8, 10, TRUE),
(2, 'MED-PARA-1000', 'Paracétamol', 'Comprimés 1000 mg', 3.20, 145, 20, TRUE),
(3, 'MED-ATOR-20', 'Atorvastatine', 'Comprimés 20 mg', 12.40, 62, 15, TRUE);

INSERT INTO rendez_vous (id, patient_id, medecin_id, date_heure, duree_minutes, motif, statut) VALUES
(1, 1, 1, '2025-06-12 09:00:00', 30, 'Suivi cardiologique', 'TERMINE'),
(2, 2, 1, '2025-06-12 10:00:00', 45, 'Première consultation', 'CONFIRME'),
(3, 3, 1, '2025-06-12 11:15:00', 30, 'Contrôle post-opératoire', 'CONFIRME');

INSERT INTO prescription (id, rendez_vous_id, medecin_id, date_prescription, instructions, validee, override_allergie) VALUES
(1, 1, 1, '2025-06-12', 'Prendre pendant le repas.', TRUE, FALSE);
INSERT INTO ligne_prescription (id, prescription_id, medicament_id, dosage, duree_jours, quantite) VALUES
(1, 1, 3, '1 comprimé le soir', 30, 30);
UPDATE medicament SET stock = stock - 30 WHERE id = 3;

INSERT INTO facture (id, numero, rendez_vous_id, montant_consultation, montant_medicaments, remise, taux_tva, total, statut) VALUES
(1, 'FAC-2025-0418', 1, 100.00, 12.40, 0.00, 20.00, 134.88, 'PARTIELLEMENT_PAYEE');
INSERT INTO paiement (id, facture_id, montant, mode, reference_paiement, date_paiement) VALUES
(1, 1, 80.00, 'CARTE', 'TPE-DEMO-001', '2025-06-12 09:45:00');

INSERT INTO notification_log (id, rendez_vous_id, destinataire, canal, type, statut, contenu, nombre_tentatives, date_envoi) VALUES
(1, 2, 'marc.dubois@example.test', 'EMAIL', 'CONFIRMATION', 'ENVOYE', 'Votre rendez-vous est confirmé.', 1, '2025-06-11 10:00:00'),
(2, 3, '+33630405060', 'SMS', 'RAPPEL_RDV', 'ECHEC', 'Rappel de votre rendez-vous MediCare+.', 3, NULL);
