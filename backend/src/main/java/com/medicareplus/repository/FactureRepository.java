package com.medicareplus.repository;
import com.medicareplus.domain.Facture;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
public interface FactureRepository extends JpaRepository<Facture, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Facture f where f.id=:id")
    Optional<Facture> findByIdForUpdate(@Param("id") Long id);
    boolean existsByRendezVousId(Long rendezVousId);
    List<Facture> findByStatutInAndCreatedAtBefore(java.util.Collection<com.medicareplus.domain.Enums.StatutPaiement> statuses, Instant before);
    @Query("select coalesce(sum(p.montant),0) from Paiement p")
    BigDecimal totalRevenue();
}
