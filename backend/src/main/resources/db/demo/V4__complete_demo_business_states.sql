-- Extend the immutable demo story so every lifecycle state can be exercised.
-- Dates are relative to first migration execution, keeping future records useful on a clean install.
SET @next_monday = TIMESTAMP(
    DATE_ADD(CURRENT_DATE, INTERVAL IF(MOD(7 - WEEKDAY(CURRENT_DATE), 7) = 0, 7, MOD(7 - WEEKDAY(CURRENT_DATE), 7)) DAY),
    '09:00:00'
);
SET @next_tuesday = TIMESTAMP(
    DATE_ADD(CURRENT_DATE, INTERVAL IF(MOD(8 - WEEKDAY(CURRENT_DATE), 7) = 0, 7, MOD(8 - WEEKDAY(CURRENT_DATE), 7)) DAY),
    '14:30:00'
);
SET @next_thursday = TIMESTAMP(
    DATE_ADD(CURRENT_DATE, INTERVAL IF(MOD(10 - WEEKDAY(CURRENT_DATE), 7) = 0, 7, MOD(10 - WEEKDAY(CURRENT_DATE), 7)) DAY),
    '08:30:00'
);
SET @previous_monday = TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL (WEEKDAY(CURRENT_DATE) + 7) DAY), '09:00:00');
SET @previous_tuesday = TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL (MOD(WEEKDAY(CURRENT_DATE) + 6, 7) + 7) DAY), '14:00:00');
SET @previous_thursday = TIMESTAMP(DATE_SUB(CURRENT_DATE, INTERVAL (MOD(WEEKDAY(CURRENT_DATE) + 4, 7) + 7) DAY), '09:00:00');

UPDATE rendez_vous SET date_heure = @previous_monday WHERE id = 1;
UPDATE rendez_vous SET date_heure = @next_monday, statut = 'CONFIRME' WHERE id = 2;
UPDATE rendez_vous SET date_heure = @next_tuesday, statut = 'CONFIRME' WHERE id = 3;
UPDATE prescription SET date_prescription = DATE(@previous_monday) WHERE id = 1;
UPDATE paiement SET date_paiement = DATE_ADD(@previous_monday, INTERVAL 45 MINUTE) WHERE id = 1;

INSERT INTO rendez_vous (id, patient_id, medecin_id, date_heure, duree_minutes, motif, statut) VALUES
(4, 1, 1, @next_thursday, 30, 'Contrôle de tension', 'EN_ATTENTE'),
(5, 3, 1, DATE_ADD(@next_monday, INTERVAL 90 MINUTE), 30, 'Consultation annulée par la patiente', 'ANNULE'),
(6, 2, 1, @previous_tuesday, 30, 'Suivi métabolique non honoré', 'ABSENT'),
(7, 2, 1, DATE_ADD(@previous_monday, INTERVAL 60 MINUTE), 30, 'Bilan annuel', 'TERMINE'),
(8, 3, 1, @previous_thursday, 30, 'Douleur inflammatoire et contrôle post-opératoire', 'TERMINE'),
(9, 1, 1, DATE_ADD(@previous_thursday, INTERVAL 60 MINUTE), 30, 'Électrocardiogramme de contrôle', 'TERMINE'),
(10, 2, 1, DATE_ADD(@previous_tuesday, INTERVAL 60 MINUTE), 30, 'Consultation de suivi pour test de facturation', 'TERMINE');

INSERT INTO medicament (id, code, nom, description, prix, stock, seuil_alerte, actif) VALUES
(4, 'MED-IBU-400', 'Ibuprofène', 'Comprimés 400 mg', 4.50, 50, 10, TRUE);

INSERT INTO prescription
    (id, rendez_vous_id, medecin_id, date_prescription, instructions, validee, override_allergie, motif_override_allergie)
VALUES
    (2, 8, 1, DATE(@previous_thursday), 'Prendre après le repas et arrêter en cas de réaction.', TRUE, TRUE,
     'Bénéfice clinique confirmé par le médecin après évaluation de l’allergie déclarée.');
INSERT INTO ligne_prescription (id, prescription_id, medicament_id, dosage, duree_jours, quantite) VALUES
    (2, 2, 4, '1 comprimé matin et soir', 6, 12);
UPDATE medicament SET stock = stock - 12 WHERE id = 4;

-- Correct the original partial invoice so medication quantity and unit price agree.
UPDATE facture
SET montant_medicaments = 372.00, total = 566.40, statut = 'PARTIELLEMENT_PAYEE'
WHERE id = 1;

INSERT INTO facture
    (id, numero, rendez_vous_id, montant_consultation, montant_medicaments, remise, taux_tva, total, statut)
VALUES
    (2, 'FAC-DEMO-EN-ATTENTE', 7, 100.00, 0.00, 0.00, 20.00, 120.00, 'EN_ATTENTE'),
    (3, 'FAC-DEMO-PAYEE', 8, 100.00, 54.00, 0.00, 20.00, 184.80, 'PAYEE'),
    (4, 'FAC-DEMO-NON-PAYEE', 9, 100.00, 0.00, 0.00, 20.00, 120.00, 'NON_PAYEE');

INSERT INTO paiement (id, facture_id, montant, mode, reference_paiement, date_paiement) VALUES
    (2, 3, 100.00, 'CARTE', 'TPE-DEMO-002', DATE_ADD(@previous_thursday, INTERVAL 45 MINUTE)),
    (3, 3, 84.80, 'VIREMENT', 'VIR-DEMO-003', DATE_ADD(@previous_thursday, INTERVAL 1 DAY));

UPDATE notification_log
SET date_envoi = DATE_SUB(@next_monday, INTERVAL 1 DAY)
WHERE id = 1;
INSERT INTO notification_log
    (id, rendez_vous_id, destinataire, canal, type, statut, contenu, nombre_tentatives, erreur, date_envoi)
VALUES
    (3, 5, 'lina.moreau@example.test', 'EMAIL', 'ANNULATION', 'ENVOYE',
     'Votre rendez-vous MediCare+ a été annulé.', 1, NULL, DATE_SUB(@next_monday, INTERVAL 2 DAY));
