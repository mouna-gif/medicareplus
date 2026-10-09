CREATE TABLE utilisateur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    role ENUM('ADMIN','MEDECIN','SECRETAIRE') NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    refresh_token_hash VARCHAR(255) NULL,
    refresh_token_expires_at TIMESTAMP NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    INDEX idx_utilisateur_role_actif (role, actif)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE medecin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id BIGINT NOT NULL UNIQUE,
    matricule VARCHAR(50) NOT NULL UNIQUE,
    specialite VARCHAR(120) NOT NULL,
    telephone VARCHAR(30),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_medecin_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE disponibilite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medecin_id BIGINT NOT NULL,
    jour_semaine ENUM('LUNDI','MARDI','MERCREDI','JEUDI','VENDREDI','SAMEDI','DIMANCHE') NOT NULL,
    heure_debut TIME NOT NULL, heure_fin TIME NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT chk_disponibilite_heures CHECK (heure_fin > heure_debut),
    CONSTRAINT fk_disponibilite_medecin FOREIGN KEY (medecin_id) REFERENCES medecin(id),
    INDEX idx_disponibilite_medecin_jour (medecin_id, jour_semaine, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_patient VARCHAR(30) NOT NULL UNIQUE, nom VARCHAR(100) NOT NULL, prenom VARCHAR(100) NOT NULL,
    date_naissance DATE NOT NULL, sexe VARCHAR(20), email VARCHAR(190), telephone VARCHAR(30), adresse VARCHAR(500),
    groupe_sanguin VARCHAR(10), archive BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    INDEX idx_patient_nom (nom, prenom), INDEX idx_patient_archive (archive)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dossier_medical (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, patient_id BIGINT NOT NULL UNIQUE,
    antecedents TEXT, allergies TEXT, notes TEXT,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_dossier_patient FOREIGN KEY (patient_id) REFERENCES patient(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE rendez_vous (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, patient_id BIGINT NOT NULL, medecin_id BIGINT NOT NULL,
    date_heure DATETIME(6) NOT NULL, duree_minutes INT NOT NULL, motif VARCHAR(500) NOT NULL,
    statut ENUM('EN_ATTENTE','CONFIRME','ANNULE','TERMINE','ABSENT') NOT NULL DEFAULT 'EN_ATTENTE',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT chk_rdv_duree CHECK (duree_minutes BETWEEN 10 AND 480),
    CONSTRAINT fk_rdv_patient FOREIGN KEY (patient_id) REFERENCES patient(id),
    CONSTRAINT fk_rdv_medecin FOREIGN KEY (medecin_id) REFERENCES medecin(id),
    INDEX idx_rdv_medecin_date (medecin_id, date_heure), INDEX idx_rdv_patient_date (patient_id, date_heure), INDEX idx_rdv_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE medicament (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(50) NOT NULL UNIQUE, nom VARCHAR(150) NOT NULL,
    description VARCHAR(500), prix DECIMAL(12,2) NOT NULL, stock INT NOT NULL, seuil_alerte INT NOT NULL DEFAULT 10,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT chk_medicament_prix CHECK (prix >= 0), CONSTRAINT chk_medicament_stock CHECK (stock >= 0),
    INDEX idx_medicament_nom (nom), INDEX idx_medicament_stock (stock, seuil_alerte)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE prescription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, rendez_vous_id BIGINT NOT NULL UNIQUE, medecin_id BIGINT NOT NULL,
    date_prescription DATE NOT NULL, instructions TEXT, validee BOOLEAN NOT NULL DEFAULT FALSE, override_allergie BOOLEAN NOT NULL DEFAULT FALSE,
    motif_override_allergie VARCHAR(500),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_prescription_rdv FOREIGN KEY (rendez_vous_id) REFERENCES rendez_vous(id),
    CONSTRAINT fk_prescription_medecin FOREIGN KEY (medecin_id) REFERENCES medecin(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ligne_prescription (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, prescription_id BIGINT NOT NULL, medicament_id BIGINT NOT NULL,
    dosage VARCHAR(150) NOT NULL, duree_jours INT NOT NULL, quantite INT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT chk_ligne_quantite CHECK (quantite > 0), CONSTRAINT chk_ligne_duree CHECK (duree_jours > 0),
    CONSTRAINT fk_ligne_prescription FOREIGN KEY (prescription_id) REFERENCES prescription(id) ON DELETE CASCADE,
    CONSTRAINT fk_ligne_medicament FOREIGN KEY (medicament_id) REFERENCES medicament(id),
    UNIQUE KEY uk_prescription_medicament (prescription_id, medicament_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE facture (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, numero VARCHAR(40) NOT NULL UNIQUE, rendez_vous_id BIGINT NOT NULL UNIQUE,
    montant_consultation DECIMAL(12,2) NOT NULL, montant_medicaments DECIMAL(12,2) NOT NULL DEFAULT 0,
    remise DECIMAL(12,2) NOT NULL DEFAULT 0, taux_tva DECIMAL(5,2) NOT NULL DEFAULT 0, total DECIMAL(12,2) NOT NULL,
    statut ENUM('EN_ATTENTE','PARTIELLEMENT_PAYEE','PAYEE','NON_PAYEE') NOT NULL DEFAULT 'EN_ATTENTE',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT chk_facture_montants CHECK (montant_consultation >= 0 AND montant_medicaments >= 0 AND remise >= 0 AND total >= 0),
    CONSTRAINT fk_facture_rdv FOREIGN KEY (rendez_vous_id) REFERENCES rendez_vous(id), INDEX idx_facture_statut (statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE paiement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, facture_id BIGINT NOT NULL, montant DECIMAL(12,2) NOT NULL,
    mode ENUM('ESPECES','CARTE','VIREMENT','EN_LIGNE') NOT NULL, reference_paiement VARCHAR(100), date_paiement DATETIME(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT chk_paiement_montant CHECK (montant > 0), CONSTRAINT fk_paiement_facture FOREIGN KEY (facture_id) REFERENCES facture(id),
    INDEX idx_paiement_facture_date (facture_id, date_paiement)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, rendez_vous_id BIGINT NULL, destinataire VARCHAR(190) NOT NULL,
    canal ENUM('EMAIL','SMS') NOT NULL, type ENUM('RAPPEL_RDV','NOUVEAU_RDV','CONFIRMATION','ANNULATION') NOT NULL,
    statut ENUM('ENVOYE','ECHEC') NOT NULL, contenu TEXT NOT NULL, nombre_tentatives INT NOT NULL DEFAULT 1,
    erreur VARCHAR(1000), date_envoi DATETIME(6),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by VARCHAR(100) NOT NULL DEFAULT 'system', updated_by VARCHAR(100) NOT NULL DEFAULT 'system',
    CONSTRAINT fk_notification_rdv FOREIGN KEY (rendez_vous_id) REFERENCES rendez_vous(id), INDEX idx_notification_statut (statut, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
