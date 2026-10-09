package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.domain.DossierMedical;
import com.medicareplus.repository.DossierMedicalRepository;
import com.medicareplus.repository.RendezVousRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class DossierMedicalService {
    private final DossierMedicalRepository repository;
    private final RendezVousRepository rendezVous;
    private final AuthorizationService authorization;
    @Transactional(readOnly = true) public DossierMedicalView getByPatient(Long patientId) { authorization.requirePatientAccess(patientId); return view(repository.findByPatientId(patientId).orElseThrow()); }
    @Transactional public DossierMedicalView update(Long patientId, DossierMedicalRequest request) {
        authorization.requirePatientAccess(patientId);
        DossierMedical dossier = repository.findByPatientId(patientId).orElseThrow();
        dossier.setAntecedents(request.antecedents()); dossier.setAllergies(request.allergies()); dossier.setNotes(request.notes());
        return view(dossier);
    }
    @Transactional(readOnly = true)
    public List<HistoriqueMedicalView> history(Long patientId) {
        authorization.requirePatientAccess(patientId);
        return rendezVous.findByPatientIdOrderByDateHeureDesc(patientId).stream()
            .map(rdv -> new HistoriqueMedicalView(rdv.getDateHeure(), "CONSULTATION", rdv.getMotif(), rdv.getStatut().name()))
            .toList();
    }
    private DossierMedicalView view(DossierMedical d) { return new DossierMedicalView(d.getId(),d.getPatient().getId(),d.getPatient().getPrenom()+" "+d.getPatient().getNom(),d.getAntecedents(),d.getAllergies(),d.getNotes()); }
}
