package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.medicareplus.domain.*;
import com.medicareplus.domain.Enums.*;
import com.medicareplus.repository.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.validation.Validation;

@ExtendWith(MockitoExtension.class)
class BusinessRulesServiceTest {
    @Mock PatientRepository patients;
    @Mock DossierMedicalRepository dossiers;
    @Mock AuthorizationService authorization;
    @Mock RendezVousRepository rendezVous;
    @Mock MedecinRepository medecins;
    @Mock DisponibiliteRepository disponibilites;
    @Mock PrescriptionRepository prescriptions;
    @Mock MedicamentRepository medicaments;
    @Mock FactureRepository factures;
    @Mock PaiementRepository paiements;

    @Test
    void patientBirthDateMustBeInThePast() {
        PatientRequest request = new PatientRequest("Durand", "Camille", LocalDate.now().plusDays(1),
            "F", "camille@example.test", null, null, null);

        assertThat(Validation.buildDefaultValidatorFactory().getValidator().validate(request))
            .anyMatch(violation -> violation.getPropertyPath().toString().equals("dateNaissance"));
    }

    @Test
    void patientCreationAlsoPersistsMedicalRecord() {
        PatientService service = new PatientService(patients, dossiers, authorization);
        PatientRequest request = new PatientRequest("Durand", "Camille", LocalDate.of(1990, 5, 14),
            "F", "camille@example.test", "+33600000000", null, "A+");
        when(patients.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(patients.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient patient = invocation.getArgument(0);
            patient.setId(11L);
            return patient;
        });

        PatientView created = service.create(request);

        assertThat(created.id()).isEqualTo(11L);
        assertThat(created.archive()).isFalse();
        verify(dossiers).save(argThat(dossier -> dossier.getPatient().getId().equals(11L)));
    }

    @Test
    void appointmentConflictIsRejectedBeforePersistence() {
        PlanningService service = new PlanningService(rendezVous, patients, medecins, disponibilites, authorization);
        LocalDateTime start = LocalDateTime.now().plusDays(14).withHour(9).withMinute(0).withSecond(0).withNano(0);
        RendezVousRequest request = new RendezVousRequest(1L, 1L, start, 30, "Suivi");
        Patient patient = new Patient();
        patient.setId(1L);
        Medecin doctor = new Medecin();
        doctor.setId(1L);
        when(medecins.findByIdForUpdate(1L)).thenReturn(Optional.of(doctor));
        when(patients.findByIdForUpdate(1L)).thenReturn(Optional.of(patient));
        when(disponibilites.covers(eq(1L), any(), any(), any())).thenReturn(true);
        when(rendezVous.hasConflict(eq(1L), eq(1L), eq(start), eq(start.plusMinutes(30)))).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Conflit");
        verify(rendezVous, never()).save(any());
    }

    @Test
    void allergyRequiresExplicitOverrideAndDoesNotDecrementStockOnFailure() {
        PrescriptionService service = new PrescriptionService(prescriptions, rendezVous, medicaments, dossiers, authorization);
        RendezVous appointment = confirmedAppointment();
        Medicament medication = medication(4L, "Ibuprofène", 20, 10);
        DossierMedical record = new DossierMedical();
        record.setAllergies("Ibuprofène");
        when(rendezVous.findById(8L)).thenReturn(Optional.of(appointment));
        when(dossiers.findByPatientId(3L)).thenReturn(Optional.of(record));
        when(medicaments.findByIdForUpdate(4L)).thenReturn(Optional.of(medication));
        PrescriptionRequest request = new PrescriptionRequest(8L, "Après le repas", false, null,
            List.of(new LignePrescriptionRequest(4L, "1 comprimé", 6, 12)));

        assertThatThrownBy(() -> service.validate(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("allergie");
        assertThat(medication.getStock()).isEqualTo(20);
        verify(prescriptions, never()).save(any());
    }

    @Test
    void explicitAllergyOverrideDecrementsStockAndReportsLowStock() {
        PrescriptionService service = new PrescriptionService(prescriptions, rendezVous, medicaments, dossiers, authorization);
        RendezVous appointment = confirmedAppointment();
        Medicament medication = medication(4L, "Ibuprofène", 12, 5);
        DossierMedical record = new DossierMedical();
        record.setAllergies("Ibuprofène");
        when(rendezVous.findById(8L)).thenReturn(Optional.of(appointment));
        when(dossiers.findByPatientId(3L)).thenReturn(Optional.of(record));
        when(medicaments.findByIdForUpdate(4L)).thenReturn(Optional.of(medication));
        PrescriptionRequest request = new PrescriptionRequest(8L, "Après le repas", true,
            "Bénéfice clinique confirmé par le médecin.",
            List.of(new LignePrescriptionRequest(4L, "1 comprimé", 6, 8)));

        PrescriptionView result = service.validate(request);

        assertThat(medication.getStock()).isEqualTo(4);
        assertThat(result.overrideAllergie()).isTrue();
        assertThat(result.lignes().getFirst().stockFaible()).isTrue();
        assertThat(result.avertissements()).hasSize(2);
        verify(prescriptions).save(any(Prescription.class));
    }

    @Test
    void invoiceCalculatesConsultationVatAndMedicationAmounts() {
        FactureService service = new FactureService(factures, rendezVous, prescriptions);
        ReflectionTestUtils.setField(service, "defaultConsultationAmount", new BigDecimal("100.00"));
        RendezVous appointment = confirmedAppointment();
        appointment.setStatut(StatutRendezVous.TERMINE);
        Prescription prescription = new Prescription();
        LignePrescription line = new LignePrescription();
        line.setMedicament(medication(2L, "Paracétamol", 100, 10));
        line.getMedicament().setPrix(new BigDecimal("3.20"));
        line.setQuantite(10);
        prescription.getLignes().add(line);
        when(rendezVous.findById(8L)).thenReturn(Optional.of(appointment));
        when(factures.existsByRendezVousId(8L)).thenReturn(false);
        when(prescriptions.findValidatedByRendezVousId(8L)).thenReturn(Optional.of(prescription));
        when(factures.save(any(Facture.class))).thenAnswer(invocation -> {
            Facture invoice = invocation.getArgument(0);
            invoice.setId(5L);
            return invoice;
        });

        FactureView result = service.create(new FactureRequest(8L, BigDecimal.ZERO, new BigDecimal("20.00")));

        assertThat(result.montantConsultation()).isEqualByComparingTo("100.00");
        assertThat(result.montantMedicaments()).isEqualByComparingTo("32.00");
        assertThat(result.total()).isEqualByComparingTo("158.40");
        assertThat(result.statut()).isEqualTo(StatutPaiement.EN_ATTENTE);
    }

    @Test
    void partialPaymentUpdatesStatusAndOverpaymentIsRejected() {
        PaiementService service = new PaiementService(factures, paiements);
        Facture invoice = new Facture();
        invoice.setId(5L);
        invoice.setTotal(new BigDecimal("120.00"));
        invoice.setStatut(StatutPaiement.EN_ATTENTE);
        when(factures.findByIdForUpdate(5L)).thenReturn(Optional.of(invoice));
        when(paiements.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement payment = invocation.getArgument(0);
            payment.setId(9L);
            return payment;
        });

        PaiementView payment = service.register(5L, new PaiementRequest(new BigDecimal("50.00"), ModePaiement.CARTE, "TEST"));

        assertThat(payment.soldeRestant()).isEqualByComparingTo("70.00");
        assertThat(payment.statutFacture()).isEqualTo(StatutPaiement.PARTIELLEMENT_PAYEE);
        assertThatThrownBy(() -> service.register(5L,
            new PaiementRequest(new BigDecimal("121.00"), ModePaiement.ESPECES, null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("dépasse");
    }

    private RendezVous confirmedAppointment() {
        Patient patient = new Patient();
        patient.setId(3L);
        Medecin doctor = new Medecin();
        doctor.setId(1L);
        RendezVous appointment = new RendezVous();
        appointment.setId(8L);
        appointment.setPatient(patient);
        appointment.setMedecin(doctor);
        appointment.setStatut(StatutRendezVous.CONFIRME);
        return appointment;
    }

    private Medicament medication(Long id, String name, int stock, int threshold) {
        Medicament medication = new Medicament();
        medication.setId(id);
        medication.setNom(name);
        medication.setStock(stock);
        medication.setSeuilAlerte(threshold);
        medication.setPrix(BigDecimal.ONE);
        return medication;
    }
}
