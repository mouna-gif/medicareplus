package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.domain.*;
import com.medicareplus.repository.*;
import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patients;
    private final DossierMedicalRepository dossiers;
    private final AuthorizationService authorization;

    @Transactional(readOnly = true)
    public Page<PatientView> search(String query, Pageable pageable) {
        Page<Patient> result = authorization.isDoctor()
            ? patients.searchForDoctor(authorization.username(), query, pageable)
            : patients.findByArchiveFalseAndNomContainingIgnoreCaseOrArchiveFalseAndPrenomContainingIgnoreCase(query, query, pageable);
        return result.map(this::view);
    }
    @Transactional(readOnly = true)
    public PatientView get(Long id) { authorization.requirePatientAccess(id); return view(patients.findById(id).orElseThrow()); }
    @Transactional
    public PatientView create(PatientRequest request) {
        requireUniqueEmail(request.email(), null);
        Patient patient = new Patient();
        patient.setNumeroPatient("PAT-" + Year.now().getValue() + "-" + ThreadLocalRandom.current().nextInt(100000, 999999));
        apply(patient, request);
        patients.save(patient);
        DossierMedical dossier = new DossierMedical();
        dossier.setPatient(patient);
        dossiers.save(dossier);
        return view(patient);
    }
    @Transactional
    public PatientView update(Long id, PatientRequest request) {
        Patient patient = patients.findById(id).orElseThrow();
        if (patient.isArchive()) throw new IllegalArgumentException("Un patient archivé ne peut pas être modifié");
        requireUniqueEmail(request.email(), id);
        apply(patient, request);
        return view(patient);
    }
    @Transactional
    public void archive(Long id) { patients.findById(id).orElseThrow().setArchive(true); }

    private void apply(Patient p, PatientRequest r) {
        p.setNom(r.nom()); p.setPrenom(r.prenom()); p.setDateNaissance(r.dateNaissance()); p.setSexe(r.sexe());
        p.setEmail(r.email()); p.setTelephone(r.telephone()); p.setAdresse(r.adresse()); p.setGroupeSanguin(r.groupeSanguin());
    }
    private void requireUniqueEmail(String email, Long currentId) {
        if (email == null || email.isBlank()) return;
        boolean exists = currentId == null ? patients.existsByEmailIgnoreCase(email) : patients.existsByEmailIgnoreCaseAndIdNot(email, currentId);
        if (exists) throw new IllegalArgumentException("Cette adresse e-mail est déjà utilisée par un patient");
    }
    private PatientView view(Patient p) {
        return new PatientView(p.getId(), p.getNumeroPatient(), p.getNom(), p.getPrenom(), p.getDateNaissance(),
            p.getSexe(), p.getEmail(), p.getTelephone(), p.getAdresse(), p.getGroupeSanguin(), p.isArchive());
    }
}
