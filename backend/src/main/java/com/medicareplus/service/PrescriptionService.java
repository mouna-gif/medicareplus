package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.domain.*;
import com.medicareplus.domain.Enums.StatutRendezVous;
import com.medicareplus.repository.*;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class PrescriptionService {
    private final PrescriptionRepository prescriptions;
    private final RendezVousRepository rendezVous;
    private final MedicamentRepository medicaments;
    private final DossierMedicalRepository dossiers;
    private final AuthorizationService authorization;

    @Transactional
    public PrescriptionView validate(PrescriptionRequest request) {
        RendezVous rdv = rendezVous.findById(request.rendezVousId()).orElseThrow();
        authorization.requireDoctorIdentity(rdv.getMedecin().getId());
        requirePrescribable(rdv);
        Prescription prescription = new Prescription();
        prescription.setRendezVous(rdv); prescription.setMedecin(rdv.getMedecin()); prescription.setDatePrescription(LocalDate.now());
        return applyAndSave(prescription, request, rdv);
    }

    @Transactional
    public PrescriptionView replace(Long id, PrescriptionRequest request) {
        Prescription prescription = prescriptions.findById(id).orElseThrow();
        authorization.requireDoctorIdentity(prescription.getMedecin().getId());
        if (!prescription.getRendezVous().getId().equals(request.rendezVousId())) {
            throw new IllegalArgumentException("Le rendez-vous d'une prescription ne peut pas être remplacé");
        }
        requirePrescribable(prescription.getRendezVous());
        restoreStock(prescription);
        prescription.getLignes().clear();
        prescriptions.flush();
        return applyAndSave(prescription, request, prescription.getRendezVous());
    }

    @Transactional
    public void delete(Long id) {
        Prescription prescription = prescriptions.findById(id).orElseThrow();
        authorization.requireDoctorIdentity(prescription.getMedecin().getId());
        restoreStock(prescription);
        prescriptions.delete(prescription);
    }

    private PrescriptionView applyAndSave(Prescription prescription, PrescriptionRequest request, RendezVous rdv) {
        String allergies = dossiers.findByPatientId(rdv.getPatient().getId()).map(DossierMedical::getAllergies).orElse("");
        prescription.setInstructions(request.instructions()); prescription.setOverrideAllergie(request.overrideAllergie());
        prescription.setMotifOverrideAllergie(request.motifOverrideAllergie());
        List<LignePrescriptionView> views = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        for (LignePrescriptionRequest line : request.lignes()) {
            Medicament medication = medicaments.findByIdForUpdate(line.medicamentId()).orElseThrow();
            if (medication.getStock() < line.quantite()) throw new IllegalArgumentException("Stock insuffisant pour " + medication.getNom());
            boolean allergyConflict = allergies != null && !allergies.isBlank() && allergies.toLowerCase().contains(medication.getNom().toLowerCase());
            if (allergyConflict && (!request.overrideAllergie() || request.motifOverrideAllergie() == null || request.motifOverrideAllergie().isBlank())) {
                throw new IllegalArgumentException("Conflit d'allergie détecté pour " + medication.getNom() + "; un motif de dérogation explicite est requis");
            }
            if (allergyConflict) warnings.add("Dérogation d'allergie confirmée pour " + medication.getNom());
            medication.setStock(medication.getStock() - line.quantite());
            LignePrescription entityLine = new LignePrescription();
            entityLine.setPrescription(prescription); entityLine.setMedicament(medication); entityLine.setDosage(line.dosage());
            entityLine.setDureeJours(line.dureeJours()); entityLine.setQuantite(line.quantite());
            prescription.getLignes().add(entityLine);
            boolean lowStock = medication.getStock() <= medication.getSeuilAlerte();
            if (lowStock) warnings.add("Stock faible pour " + medication.getNom() + " : " + medication.getStock() + " unité(s)");
            views.add(new LignePrescriptionView(medication.getId(), medication.getNom(), line.dosage(), line.dureeJours(), line.quantite(), lowStock));
        }
        prescription.setValidee(true);
        prescriptions.save(prescription);
        return new PrescriptionView(prescription.getId(), rdv.getId(), prescription.getDatePrescription(),
            prescription.getInstructions(), true, prescription.isOverrideAllergie(), views, warnings);
    }

    private void restoreStock(Prescription prescription) {
        prescription.getLignes().forEach(line -> {
            Medicament medication = medicaments.findByIdForUpdate(line.getMedicament().getId()).orElseThrow();
            medication.setStock(medication.getStock() + line.getQuantite());
        });
    }
    private void requirePrescribable(RendezVous rdv) {
        if (rdv.getStatut() != StatutRendezVous.CONFIRME && rdv.getStatut() != StatutRendezVous.TERMINE) {
            throw new IllegalArgumentException("Une prescription exige un rendez-vous confirmé ou terminé");
        }
    }
}
