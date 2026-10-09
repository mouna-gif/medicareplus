package com.medicareplus.repository;
import com.medicareplus.domain.DossierMedical;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DossierMedicalRepository extends JpaRepository<DossierMedical, Long> {
    Optional<DossierMedical> findByPatientId(Long patientId);
}
