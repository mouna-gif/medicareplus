package com.medicareplus.domain;

import com.medicareplus.domain.Enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "notification_log", indexes = @Index(name = "idx_notification_statut", columnList = "statut, created_at"))
@Getter @Setter @NoArgsConstructor
public class NotificationLog extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "rendez_vous_id") private RendezVous rendezVous;
    @Column(nullable = false, length = 190) private String destinataire;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private CanalNotification canal;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TypeNotification type;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private StatutEnvoi statut;
    @Column(nullable = false, columnDefinition = "TEXT") private String contenu;
    @Column(name = "nombre_tentatives", nullable = false) private int nombreTentatives = 1;
    @Column(length = 1000) private String erreur;
    @Column(name = "date_envoi") private LocalDateTime dateEnvoi;
}
