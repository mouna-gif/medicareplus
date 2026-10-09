package com.medicareplus.domain;

import com.medicareplus.domain.Enums.StatutRendezVous;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "rendez_vous", indexes = {
    @Index(name = "idx_rdv_medecin_date", columnList = "medecin_id, date_heure"),
    @Index(name = "idx_rdv_patient_date", columnList = "patient_id, date_heure"),
    @Index(name = "idx_rdv_statut", columnList = "statut")
})
@Getter @Setter @NoArgsConstructor
public class RendezVous extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "patient_id", nullable = false) private Patient patient;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "medecin_id", nullable = false) private Medecin medecin;
    @Column(name = "date_heure", nullable = false) private LocalDateTime dateHeure;
    @Column(name = "duree_minutes", nullable = false) private int dureeMinutes;
    @Column(nullable = false, length = 500) private String motif;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private StatutRendezVous statut = StatutRendezVous.EN_ATTENTE;
}
