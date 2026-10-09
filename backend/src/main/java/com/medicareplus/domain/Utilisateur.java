package com.medicareplus.domain;

import com.medicareplus.domain.Enums.Role;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "utilisateur", indexes = @Index(name = "idx_utilisateur_role_actif", columnList = "role, actif"))
@Getter @Setter @NoArgsConstructor
public class Utilisateur extends AuditableEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 100) private String username;
    @Column(nullable = false, unique = true, length = 190) private String email;
    @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
    @Column(nullable = false, length = 100) private String nom;
    @Column(nullable = false, length = 100) private String prenom;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Role role;
    @Column(nullable = false) private boolean actif = true;
    @Column(name = "refresh_token_hash", length = 255) private String refreshTokenHash;
    @Column(name = "refresh_token_expires_at") private Instant refreshTokenExpiresAt;
}
