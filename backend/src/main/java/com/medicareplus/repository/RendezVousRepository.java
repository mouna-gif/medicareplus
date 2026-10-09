package com.medicareplus.repository;
import com.medicareplus.domain.RendezVous;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    @Query(value = "SELECT COUNT(*) > 0 FROM rendez_vous r WHERE r.statut <> 'ANNULE' AND (r.medecin_id=:medecinId OR r.patient_id=:patientId) AND r.date_heure<:fin AND DATE_ADD(r.date_heure, INTERVAL r.duree_minutes MINUTE)>:debut", nativeQuery = true)
    boolean hasConflict(@Param("medecinId") Long medecinId, @Param("patientId") Long patientId, @Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
    boolean existsByMedecinIdAndDateHeureAfterAndStatutNot(Long medecinId, LocalDateTime after, com.medicareplus.domain.Enums.StatutRendezVous statut);
    List<RendezVous> findByStatutAndDateHeureBetween(com.medicareplus.domain.Enums.StatutRendezVous statut, LocalDateTime start, LocalDateTime end);
    List<RendezVous> findByPatientIdOrderByDateHeureDesc(Long patientId);
    long countByStatut(com.medicareplus.domain.Enums.StatutRendezVous statut);
    boolean existsByPatientIdAndMedecinUtilisateurUsername(Long patientId, String username);
}
