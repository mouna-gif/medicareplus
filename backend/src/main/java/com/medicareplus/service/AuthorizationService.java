package com.medicareplus.service;

import com.medicareplus.domain.Medecin;
import com.medicareplus.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final RendezVousRepository rendezVous;
    private final MedecinRepository medecins;

    public String username() { return authentication().getName(); }
    public boolean isDoctor() { return hasRole("ROLE_MEDECIN"); }
    public boolean isAdmin() { return hasRole("ROLE_ADMIN"); }
    public void requirePatientAccess(Long patientId) {
        if (isDoctor() && !rendezVous.existsByPatientIdAndMedecinUtilisateurUsername(patientId, username())) {
            throw new AccessDeniedException("Ce patient n'est pas associé au médecin authentifié");
        }
    }
    public void requireDoctorIdentity(Long doctorId) {
        if (!isDoctor()) return;
        Medecin current = medecins.findByUtilisateurUsername(username()).orElseThrow(() -> new AccessDeniedException("Profil médecin introuvable"));
        if (!current.getId().equals(doctorId)) throw new AccessDeniedException("Accès à un autre médecin interdit");
    }
    private boolean hasRole(String role) { return authentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role)); }
    private Authentication authentication() { return SecurityContextHolder.getContext().getAuthentication(); }
}
