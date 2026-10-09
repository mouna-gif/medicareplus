package com.medicareplus.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity @Table(name = "prescription")
@Getter @Setter @NoArgsConstructor
public class Prescription extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "rendez_vous_id", nullable = false, unique = true) private RendezVous rendezVous;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "medecin_id", nullable = false) private Medecin medecin;
    @Column(name = "date_prescription", nullable = false) private LocalDate datePrescription;
    @Column(columnDefinition = "TEXT") private String instructions;
    @Column(nullable = false) private boolean validee;
    @Column(name = "override_allergie", nullable = false) private boolean overrideAllergie;
    @Column(name = "motif_override_allergie", length = 500) private String motifOverrideAllergie;
    @OneToMany(mappedBy = "prescription", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LignePrescription> lignes = new ArrayList<>();
}
