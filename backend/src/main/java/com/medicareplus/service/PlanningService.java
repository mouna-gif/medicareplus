package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.domain.*;
import com.medicareplus.domain.Enums.StatutRendezVous;
import com.medicareplus.domain.Enums.JourSemaine;
import com.medicareplus.repository.*;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

@Service @RequiredArgsConstructor
public class PlanningService {
    private final RendezVousRepository rendezVous;
    private final PatientRepository patients;
    private final MedecinRepository medecins;
    private final DisponibiliteRepository disponibilites;
    private final AuthorizationService authorization;

    @Transactional
    public RendezVousView create(RendezVousRequest request) {
        authorization.requireDoctorIdentity(request.medecinId());
        if (!request.dateHeure().isAfter(LocalDateTime.now())) throw new IllegalArgumentException("Un rendez-vous ne peut pas être créé dans le passé");
        LocalDateTime fin = request.dateHeure().plusMinutes(request.dureeMinutes());
        JourSemaine jour = toJourSemaine(request.dateHeure().getDayOfWeek());
        Medecin doctor = medecins.findByIdForUpdate(request.medecinId()).orElseThrow();
        Patient patient = patients.findByIdForUpdate(request.patientId()).orElseThrow();
        if (patient.isArchive()) throw new IllegalArgumentException("Un rendez-vous ne peut pas être créé pour un patient archivé");
        if (!disponibilites.covers(request.medecinId(), jour, request.dateHeure().toLocalTime(), fin.toLocalTime())) {
            throw new IllegalArgumentException("Le rendez-vous doit être compris dans une disponibilité du médecin");
        }
        if (rendezVous.hasConflict(request.medecinId(), request.patientId(), request.dateHeure(), fin)) {
            throw new IllegalArgumentException("Conflit de planning détecté pour le médecin ou le patient");
        }
        RendezVous entity = new RendezVous();
        entity.setPatient(patient);
        entity.setMedecin(doctor);
        entity.setDateHeure(request.dateHeure()); entity.setDureeMinutes(request.dureeMinutes());
        entity.setMotif(request.motif()); entity.setStatut(StatutRendezVous.EN_ATTENTE);
        return view(rendezVous.save(entity));
    }
    @Transactional
    public RendezVousView changeStatus(Long id, StatutRendezVous status) {
        RendezVous entity = rendezVous.findById(id).orElseThrow();
        authorization.requireDoctorIdentity(entity.getMedecin().getId());
        if (!validTransition(entity.getStatut(), status)) {
            throw new IllegalArgumentException("Transition de statut de rendez-vous non autorisée");
        }
        if (status == StatutRendezVous.ANNULE && entity.getDateHeure().isBefore(LocalDateTime.now().plusHours(2)) && !isAdmin()) {
            throw new IllegalArgumentException("Une annulation moins de deux heures avant le rendez-vous est réservée à un administrateur");
        }
        entity.setStatut(status);
        return view(entity);
    }
    @Transactional
    public DisponibiliteView addAvailability(DisponibiliteRequest request) {
        authorization.requireDoctorIdentity(request.medecinId());
        if (!request.heureFin().isAfter(request.heureDebut())) throw new IllegalArgumentException("L'heure de fin doit suivre l'heure de début");
        Medecin doctor = medecins.findByIdForUpdate(request.medecinId()).orElseThrow();
        if (disponibilites.overlaps(request.medecinId(), request.jourSemaine(), request.heureDebut(), request.heureFin())) {
            throw new IllegalArgumentException("Cette disponibilité chevauche une plage existante");
        }
        Disponibilite d = new Disponibilite();
        d.setMedecin(doctor); d.setJourSemaine(request.jourSemaine());
        d.setHeureDebut(request.heureDebut()); d.setHeureFin(request.heureFin()); d.setActive(true);
        disponibilites.save(d);
        return new DisponibiliteView(d.getId(), d.getMedecin().getId(), d.getJourSemaine(), d.getHeureDebut(), d.getHeureFin(), d.isActive());
    }
    private RendezVousView view(RendezVous r) {
        return new RendezVousView(r.getId(), r.getPatient().getId(), r.getPatient().getPrenom()+" "+r.getPatient().getNom(),
            r.getMedecin().getId(), "Dr. "+r.getMedecin().getUtilisateur().getNom(), r.getDateHeure(), r.getDureeMinutes(), r.getMotif(), r.getStatut());
    }
    private boolean validTransition(StatutRendezVous current, StatutRendezVous target) {
        if (current == target) return true;
        return switch (current) {
            case EN_ATTENTE -> target == StatutRendezVous.CONFIRME || target == StatutRendezVous.ANNULE;
            case CONFIRME -> target == StatutRendezVous.TERMINE || target == StatutRendezVous.ANNULE || target == StatutRendezVous.ABSENT;
            case ANNULE, TERMINE, ABSENT -> false;
        };
    }
    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
    private JourSemaine toJourSemaine(java.time.DayOfWeek day) {
        return switch (day) {
            case MONDAY -> JourSemaine.LUNDI; case TUESDAY -> JourSemaine.MARDI;
            case WEDNESDAY -> JourSemaine.MERCREDI; case THURSDAY -> JourSemaine.JEUDI;
            case FRIDAY -> JourSemaine.VENDREDI; case SATURDAY -> JourSemaine.SAMEDI;
            case SUNDAY -> JourSemaine.DIMANCHE;
        };
    }
}
