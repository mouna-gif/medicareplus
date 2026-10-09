package com.medicareplus.domain;

import com.medicareplus.domain.Enums.ModePaiement;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "paiement", indexes = @Index(name = "idx_paiement_facture_date", columnList = "facture_id, date_paiement"))
@Getter @Setter @NoArgsConstructor
public class Paiement extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "facture_id", nullable = false) private Facture facture;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal montant;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ModePaiement mode;
    @Column(name = "reference_paiement", length = 100) private String referencePaiement;
    @Column(name = "date_paiement", nullable = false) private LocalDateTime datePaiement;
}
