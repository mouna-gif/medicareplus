ALTER TABLE patient ADD CONSTRAINT uk_patient_email UNIQUE (email);
CREATE INDEX idx_facture_statut_creation ON facture (statut, created_at);
