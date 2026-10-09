# MediCare+ Static Verification Record

This document records the source-level audit between the JPA model and the Flyway MySQL schema. Runtime schema validation remains the responsibility of application startup with `spring.jpa.hibernate.ddl-auto=validate`.

## Common persistence contract

All 12 entities use `BIGINT AUTO_INCREMENT` / `Long` identity primary keys and inherit:

| JPA field | Java type | MySQL column | SQL type | Nullability |
| --- | --- | --- | --- | --- |
| `createdAt` | `Instant` | `created_at` | `TIMESTAMP(6)` | `NOT NULL` |
| `updatedAt` | `Instant` | `updated_at` | `TIMESTAMP(6)` | `NOT NULL` |
| `createdBy` | `String` | `created_by` | `VARCHAR(100)` | `NOT NULL` |
| `updatedBy` | `String` | `updated_by` | `VARCHAR(100)` | `NOT NULL` |

`AuditableEntity` uses `AuditingEntityListener`, and the application enables auditing with the named `AuditorAware<String>` bean.

## Entity and table parity

| Entity / table | Column parity | Keys, constraints, and indexes | Relationships |
| --- | --- | --- | --- |
| `Utilisateur` / `utilisateur` | `username VARCHAR(100)`, `email VARCHAR(190)`, `password_hash VARCHAR(255)`, `nom/prenom VARCHAR(100)`, `role ENUM`, `actif BOOLEAN`, nullable refresh hash/expiry | Unique username and email; `idx_utilisateur_role_actif`; Java defaults `actif=true` | Inverse physician relationship is intentionally not materialized; `Medecin` owns the one-to-one FK |
| `Medecin` / `medecin` | `matricule VARCHAR(50)`, `specialite VARCHAR(120)`, nullable `telephone VARCHAR(30)` | Unique `utilisateur_id` and `matricule` | Lazy mandatory `OneToOne` to `Utilisateur` |
| `Disponibilite` / `disponibilite` | `jour_semaine ENUM`, `heure_debut/fin TIME`, `active BOOLEAN` | Time-range check and `idx_disponibilite_medecin_jour`; Java default `active=true` | Lazy mandatory `ManyToOne` to `Medecin` |
| `Patient` / `patient` | Number, names, `LocalDate` birth date, optional contact/address/blood fields, `archive BOOLEAN` | Unique patient number and email; name/archive indexes; Java default `archive=false` | Medical record and appointments own their patient FKs |
| `DossierMedical` / `dossier_medical` | Nullable `TEXT` antecedents, allergies, and notes | Unique `patient_id` | Lazy mandatory `OneToOne` to `Patient` |
| `RendezVous` / `rendez_vous` | `date_heure DATETIME(6)`, duration integer, reason `VARCHAR(500)`, status enum | Duration check; doctor/date, patient/date, and status indexes; Java default `EN_ATTENTE` | Lazy mandatory `ManyToOne` to `Patient` and `Medecin` |
| `Prescription` / `prescription` | `LocalDate` prescription date, nullable instructions `TEXT`, validation/override booleans, nullable override reason | Unique `rendez_vous_id` | Lazy mandatory `OneToOne` appointment, lazy mandatory `ManyToOne` physician, lazy `OneToMany` lines with cascade and orphan removal |
| `LignePrescription` / `ligne_prescription` | Dosage `VARCHAR(150)`, duration and quantity integers | Positive duration/quantity checks; unique prescription/medication pair | Lazy mandatory `ManyToOne` to `Prescription` and `Medicament`; SQL cascade matches JPA orphan lifecycle |
| `Medicament` / `medicament` | Code/name/description, `DECIMAL(12,2)` price, integer stock/threshold, active boolean | Unique code; non-negative price/stock checks; name and stock indexes; defaults threshold 10 and active | Prescription lines own medication FKs |
| `Facture` / `facture` | Number, five decimal amount/rate fields, payment-status enum | Unique number and appointment; non-negative amount check; status and status/creation indexes; zero amount defaults and `EN_ATTENTE` aligned in Java | Lazy mandatory `OneToOne` appointment; lazy `OneToMany` payments without destructive cascade |
| `Paiement` / `paiement` | `DECIMAL(12,2)` amount, payment-mode enum, optional reference, `DATETIME(6)` date | Positive amount check and facture/date index | Lazy mandatory `ManyToOne` to `Facture` |
| `NotificationLog` / `notification_log` | Recipient, channel/type/status enums, content `TEXT`, attempts, optional error/date | Status/creation index; attempts default 1 | Optional lazy `ManyToOne` to `RendezVous` |

The 12 owning JPA relationships correspond one-for-one with the 12 Flyway foreign keys. The only destructive cascade is prescription-to-lines, matching `ON DELETE CASCADE`. Financial, clinical, patient, and notification history are not cascade-deleted.

## Enum parity

Every persisted enum uses `@Enumerated(EnumType.STRING)` and exactly matches its MySQL `ENUM` literals:

- `Role`: `ADMIN`, `MEDECIN`, `SECRETAIRE`
- `StatutRendezVous`: `EN_ATTENTE`, `CONFIRME`, `ANNULE`, `TERMINE`, `ABSENT`
- `StatutPaiement`: `EN_ATTENTE`, `PARTIELLEMENT_PAYEE`, `PAYEE`, `NON_PAYEE`
- `ModePaiement`: `ESPECES`, `CARTE`, `VIREMENT`, `EN_LIGNE`
- `CanalNotification`: `EMAIL`, `SMS`
- `TypeNotification`: `RAPPEL_RDV`, `NOUVEAU_RDV`, `CONFIRMATION`, `ANNULATION`
- `StatutEnvoi`: `ENVOYE`, `ECHEC`
- `JourSemaine`: `LUNDI`, `MARDI`, `MERCREDI`, `JEUDI`, `VENDREDI`, `SAMEDI`, `DIMANCHE`

## DTO and REST boundary

Controllers import DTO records and services only. They do not import, accept, or return classes from `com.medicareplus.domain`. Each mapped operation has an OpenAPI operation description; special `201`, `202`, and `204` responses are explicit, while shared `400`, `401`, `403`, `404`, and `409` responses are added by the OpenAPI customizer.

## Runtime verification boundary

Compilation, unit tests, resource packaging, and static parity checks can run without a database. A clean MySQL startup, Flyway execution, Hibernate runtime validation, Swagger startup, API smoke suite, and restart persistence check require a reachable MySQL 8 server. Use the documented Docker Compose stack and `scripts/verify-api.ps1`.
