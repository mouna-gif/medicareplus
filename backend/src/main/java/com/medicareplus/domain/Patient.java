package com.medicareplus.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "patient", indexes = {
    @Index(name = "idx_patient_nom", columnList = "nom, prenom"),
    @Index(name = "idx_patient_archive", columnList = "archive")
})
@Getter @Setter @NoArgsConstructor
public class Patient extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "numero_patient", nullable = false, unique = true, length = 30) private String numeroPatient;
    @Column(nullable = false, length = 100) private String nom;
    @Column(nullable = false, length = 100) private String prenom;
    @Column(name = "date_naissance", nullable = false) private LocalDate dateNaissance;
    @Column(length = 20) private String sexe;
    @Column(length = 190, unique = true) private String email;
    @Column(length = 30) private String telephone;
    @Column(length = 500) private String adresse;
    @Column(name = "groupe_sanguin", length = 10) private String groupeSanguin;
    @Column(nullable = false) private boolean archive;
}
