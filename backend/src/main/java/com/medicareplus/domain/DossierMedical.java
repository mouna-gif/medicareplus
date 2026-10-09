package com.medicareplus.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "dossier_medical")
@Getter @Setter @NoArgsConstructor
public class DossierMedical extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "patient_id", nullable = false, unique = true) private Patient patient;
    @Column(columnDefinition = "TEXT") private String antecedents;
    @Column(columnDefinition = "TEXT") private String allergies;
    @Column(columnDefinition = "TEXT") private String notes;
}
