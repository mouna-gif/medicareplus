package com.medicareplus.repository;
import com.medicareplus.domain.Prescription;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    @Query("select distinct p from Prescription p left join fetch p.lignes l left join fetch l.medicament where p.rendezVous.id=:rendezVousId and p.validee=true")
    Optional<Prescription> findValidatedByRendezVousId(@Param("rendezVousId") Long rendezVousId);
}
