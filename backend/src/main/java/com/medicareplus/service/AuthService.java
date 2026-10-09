package com.medicareplus.service;

import static com.medicareplus.api.AuthDtos.*;

import com.medicareplus.domain.Utilisateur;
import com.medicareplus.repository.UtilisateurRepository;
import com.medicareplus.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository users;
    private final JwtService jwt;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return issue(users.findByUsernameAndActifTrue(request.username()).orElseThrow());
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        if (!jwt.valid(request.refreshToken()) || !"refresh".equals(jwt.type(request.refreshToken()))) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        Utilisateur user = users.findByUsernameAndActifTrue(jwt.username(request.refreshToken())).orElseThrow();
        if (!hash(request.refreshToken()).equals(user.getRefreshTokenHash()) ||
            user.getRefreshTokenExpiresAt() == null || user.getRefreshTokenExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token revoked or expired");
        }
        return issue(user);
    }

    @Transactional
    public void logout(String username) {
        users.findByUsernameAndActifTrue(username).ifPresent(user -> {
            user.setRefreshTokenHash(null);
            user.setRefreshTokenExpiresAt(null);
        });
    }

    private AuthResponse issue(Utilisateur user) {
        String access = jwt.accessToken(user);
        String refresh = jwt.refreshToken(user);
        user.setRefreshTokenHash(hash(refresh));
        user.setRefreshTokenExpiresAt(Instant.now().plusMillis(jwt.refreshExpiration()));
        UserView view = new UserView(user.getId(), user.getUsername(), user.getPrenom() + " " + user.getNom(), user.getRole());
        return new AuthResponse(access, refresh, "Bearer", jwt.accessExpiration() / 1000, view);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) { throw new IllegalStateException(ex); }
    }
}
