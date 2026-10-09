package com.medicareplus.repository;
import com.medicareplus.domain.Medicament;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface MedicamentRepository extends JpaRepository<Medicament, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medicament m where m.id=:id")
    Optional<Medicament> findByIdForUpdate(@Param("id") Long id);
}
