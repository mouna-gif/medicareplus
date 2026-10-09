package com.medicareplus.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "medecin")
@Getter @Setter @NoArgsConstructor
public class Medecin extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "utilisateur_id", nullable = false, unique = true) private Utilisateur utilisateur;
    @Column(nullable = false, unique = true, length = 50) private String matricule;
    @Column(nullable = false, length = 120) private String specialite;
    @Column(length = 30) private String telephone;
}
