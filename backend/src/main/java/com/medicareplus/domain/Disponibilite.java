package com.medicareplus.domain;

import com.medicareplus.domain.Enums.JourSemaine;
import jakarta.persistence.*;
import java.time.LocalTime;
import lombok.*;

@Entity
@Table(name = "disponibilite", indexes = @Index(name = "idx_disponibilite_medecin_jour", columnList = "medecin_id, jour_semaine, active"))
@Getter @Setter @NoArgsConstructor
public class Disponibilite extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "medecin_id", nullable = false) private Medecin medecin;
    @Enumerated(EnumType.STRING) @Column(name = "jour_semaine", nullable = false) private JourSemaine jourSemaine;
    @Column(name = "heure_debut", nullable = false) private LocalTime heureDebut;
    @Column(name = "heure_fin", nullable = false) private LocalTime heureFin;
    @Column(nullable = false) private boolean active = true;
}
