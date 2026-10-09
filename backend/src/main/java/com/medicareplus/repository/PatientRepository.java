package com.medicareplus.repository;
import com.medicareplus.domain.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;
import jakarta.persistence.LockModeType;
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Page<Patient> findByArchiveFalseAndNomContainingIgnoreCaseOrArchiveFalseAndPrenomContainingIgnoreCase(String nom, String prenom, Pageable pageable);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    @Query("select count(p) from Patient p where p.createdAt>=:since")
    long countCreatedSince(@Param("since") Instant since);
    @Query(value="select distinct r.patient from RendezVous r where r.medecin.utilisateur.username=:username and r.patient.archive=false and (lower(r.patient.nom) like lower(concat('%',:query,'%')) or lower(r.patient.prenom) like lower(concat('%',:query,'%')))",
        countQuery="select count(distinct r.patient.id) from RendezVous r where r.medecin.utilisateur.username=:username and r.patient.archive=false and (lower(r.patient.nom) like lower(concat('%',:query,'%')) or lower(r.patient.prenom) like lower(concat('%',:query,'%')))")
    Page<Patient> searchForDoctor(@Param("username") String username, @Param("query") String query, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Patient p where p.id=:id")
    Optional<Patient> findByIdForUpdate(@Param("id") Long id);
}
