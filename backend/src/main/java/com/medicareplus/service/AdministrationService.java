package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.domain.*;
import com.medicareplus.domain.Enums.Role;
import com.medicareplus.repository.*;
import java.util.List;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class AdministrationService {
    private final UtilisateurRepository utilisateurs;
    private final MedecinRepository medecins;
    private final PasswordEncoder passwordEncoder;
    private final RendezVousRepository rendezVous;

    @Transactional(readOnly = true)
    public List<UtilisateurView> users() { return utilisateurs.findAll().stream().map(this::userView).toList(); }
    @Transactional
    public UtilisateurView createUser(UtilisateurRequest request) {
        Utilisateur user = new Utilisateur();
        user.setUsername(request.username()); user.setEmail(request.email()); user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNom(request.nom()); user.setPrenom(request.prenom()); user.setRole(request.role()); user.setActif(true);
        return userView(utilisateurs.save(user));
    }
    @Transactional
    public void deactivateUser(Long id) {
        Utilisateur user = utilisateurs.findById(id).orElseThrow();
        user.setActif(false); user.setRefreshTokenHash(null); user.setRefreshTokenExpiresAt(null);
    }
    @Transactional
    public MedecinView createDoctor(MedecinRequest request) {
        Utilisateur user = utilisateurs.findById(request.utilisateurId()).orElseThrow();
        if (user.getRole() != Role.MEDECIN) throw new IllegalArgumentException("Le compte utilisateur doit avoir le rôle MEDECIN");
        Medecin doctor = new Medecin();
        doctor.setUtilisateur(user); doctor.setMatricule(request.matricule()); doctor.setSpecialite(request.specialite()); doctor.setTelephone(request.telephone());
        medecins.save(doctor);
        return doctorView(doctor);
    }
    @Transactional(readOnly = true)
    public List<MedecinView> doctors() { return medecins.findAll().stream().map(this::doctorView).toList(); }
    @Transactional
    public void deactivateDoctor(Long id) {
        Medecin doctor = medecins.findById(id).orElseThrow();
        if (rendezVous.existsByMedecinIdAndDateHeureAfterAndStatutNot(id, LocalDateTime.now(), com.medicareplus.domain.Enums.StatutRendezVous.ANNULE)) {
            throw new IllegalArgumentException("Le médecin a des rendez-vous futurs et ne peut pas être désactivé");
        }
        doctor.getUtilisateur().setActif(false);
    }
    private UtilisateurView userView(Utilisateur u) { return new UtilisateurView(u.getId(),u.getUsername(),u.getEmail(),u.getNom(),u.getPrenom(),u.getRole(),u.isActif()); }
    private MedecinView doctorView(Medecin m) { return new MedecinView(m.getId(),m.getUtilisateur().getId(),m.getUtilisateur().getPrenom()+" "+m.getUtilisateur().getNom(),m.getMatricule(),m.getSpecialite(),m.getTelephone()); }
}
