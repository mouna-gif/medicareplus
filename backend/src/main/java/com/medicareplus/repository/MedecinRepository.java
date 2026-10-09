package com.medicareplus.repository;
import com.medicareplus.domain.Medecin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
public interface MedecinRepository extends JpaRepository<Medecin, Long> {
    Optional<Medecin> findByUtilisateurUsername(String username);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Medecin m where m.id=:id")
    Optional<Medecin> findByIdForUpdate(@Param("id") Long id);
}
