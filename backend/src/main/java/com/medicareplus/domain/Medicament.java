package com.medicareplus.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "medicament", indexes = {
    @Index(name = "idx_medicament_nom", columnList = "nom"),
    @Index(name = "idx_medicament_stock", columnList = "stock, seuil_alerte")
})
@Getter @Setter @NoArgsConstructor
public class Medicament extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 50) private String code;
    @Column(nullable = false, length = 150) private String nom;
    @Column(length = 500) private String description;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal prix;
    @Column(nullable = false) private int stock;
    @Column(name = "seuil_alerte", nullable = false) private int seuilAlerte = 10;
    @Column(nullable = false) private boolean actif = true;
}
