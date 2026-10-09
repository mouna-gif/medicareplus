package com.medicareplus.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ligne_prescription", uniqueConstraints = @UniqueConstraint(
    name = "uk_prescription_medicament", columnNames = {"prescription_id", "medicament_id"}
))
@Getter @Setter @NoArgsConstructor
public class LignePrescription extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "prescription_id", nullable = false) private Prescription prescription;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "medicament_id", nullable = false) private Medicament medicament;
    @Column(nullable = false, length = 150) private String dosage;
    @Column(name = "duree_jours", nullable = false) private int dureeJours;
    @Column(nullable = false) private int quantite;
}
