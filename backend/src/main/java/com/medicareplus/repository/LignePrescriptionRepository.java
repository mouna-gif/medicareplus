package com.medicareplus.repository;
import com.medicareplus.domain.LignePrescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface LignePrescriptionRepository extends JpaRepository<LignePrescription, Long> {
    @Query("select l.medicament.nom, sum(l.quantite) from LignePrescription l where l.prescription.validee=true group by l.medicament.id,l.medicament.nom order by sum(l.quantite) desc")
    List<Object[]> mostPrescribed(Pageable pageable);
}
