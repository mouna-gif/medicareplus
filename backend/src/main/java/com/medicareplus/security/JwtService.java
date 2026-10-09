package com.medicareplus.security;

import com.medicareplus.domain.Utilisateur;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-expiration}") long accessExpiration,
                      @Value("${app.jwt.refresh-expiration}") long refreshExpiration) {
        if (secret.length() < 32) throw new IllegalArgumentException("JWT_SECRET must contain at least 32 characters");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String accessToken(Utilisateur user) { return token(user, accessExpiration, "access"); }
    public String refreshToken(Utilisateur user) { return token(user, refreshExpiration, "refresh"); }
    public long accessExpiration() { return accessExpiration; }
    public long refreshExpiration() { return refreshExpiration; }
    public String username(String token) { return claims(token).getSubject(); }
    public String type(String token) { return claims(token).get("type", String.class); }
    public boolean valid(String token) { try { return claims(token).getExpiration().after(new Date()); } catch (RuntimeException ex) { return false; } }

    private String token(Utilisateur user, long lifetime, String type) {
        Instant now = Instant.now();
        return Jwts.builder().id(UUID.randomUUID().toString()).issuer("medicareplus-api")
            .subject(user.getUsername()).claim("role", user.getRole().name()).claim("type", type)
            .issuedAt(Date.from(now)).expiration(Date.from(now.plusMillis(lifetime))).signWith(key).compact();
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer("medicareplus-api").build().parseSignedClaims(token).getPayload();
    }
}
