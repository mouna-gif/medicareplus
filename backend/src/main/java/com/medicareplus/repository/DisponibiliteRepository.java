package com.medicareplus.repository;
import com.medicareplus.domain.Disponibilite;
import com.medicareplus.domain.Enums.JourSemaine;
import java.time.LocalTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {
    @Query("select count(d)>0 from Disponibilite d where d.medecin.id=:medecinId and d.jourSemaine=:jour and d.active=true and d.heureDebut<:fin and d.heureFin>:debut")
    boolean overlaps(@Param("medecinId") Long medecinId, @Param("jour") JourSemaine jour, @Param("debut") LocalTime debut, @Param("fin") LocalTime fin);
    @Query("select count(d)>0 from Disponibilite d where d.medecin.id=:medecinId and d.jourSemaine=:jour and d.active=true and d.heureDebut<=:debut and d.heureFin>=:fin")
    boolean covers(@Param("medecinId") Long medecinId, @Param("jour") JourSemaine jour, @Param("debut") LocalTime debut, @Param("fin") LocalTime fin);
}
