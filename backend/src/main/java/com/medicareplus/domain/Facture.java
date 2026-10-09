package com.medicareplus.domain;

import com.medicareplus.domain.Enums.StatutPaiement;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "facture", indexes = {
    @Index(name = "idx_facture_statut", columnList = "statut"),
    @Index(name = "idx_facture_statut_creation", columnList = "statut, created_at")
})
@Getter @Setter @NoArgsConstructor
public class Facture extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 40) private String numero;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "rendez_vous_id", nullable = false, unique = true) private RendezVous rendezVous;
    @Column(name = "montant_consultation", nullable = false, precision = 12, scale = 2) private BigDecimal montantConsultation;
    @Column(name = "montant_medicaments", nullable = false, precision = 12, scale = 2) private BigDecimal montantMedicaments = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal remise = BigDecimal.ZERO;
    @Column(name = "taux_tva", nullable = false, precision = 5, scale = 2) private BigDecimal tauxTva = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private StatutPaiement statut = StatutPaiement.EN_ATTENTE;
    @OneToMany(mappedBy = "facture", fetch = FetchType.LAZY) private List<Paiement> paiements = new ArrayList<>();
}
