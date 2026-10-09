package com.medicareplus.service;

import static com.medicareplus.api.AuthDtos.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.medicareplus.domain.Enums.Role;
import com.medicareplus.domain.Utilisateur;
import com.medicareplus.repository.UtilisateurRepository;
import com.medicareplus.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock UtilisateurRepository users;
    @Mock JwtService jwt;
    private AuthService service;
    private Utilisateur user;

    @BeforeEach
    void setUp() {
        service = new AuthService(authenticationManager, users, jwt);
        user = new Utilisateur();
        user.setId(7L);
        user.setUsername("medecin.demo");
        user.setPrenom("Alexandre");
        user.setNom("Martin");
        user.setRole(Role.MEDECIN);
        user.setActif(true);
    }

    @Test
    void loginAuthenticatesAgainstSpringSecurityAndReturnsRoleAndTokens() {
        when(users.findByUsernameAndActifTrue("medecin.demo")).thenReturn(Optional.of(user));
        when(jwt.accessToken(user)).thenReturn("access-token");
        when(jwt.refreshToken(user)).thenReturn("refresh-token");
        when(jwt.accessExpiration()).thenReturn(900_000L);
        when(jwt.refreshExpiration()).thenReturn(604_800_000L);

        AuthResponse response = service.login(new LoginRequest("medecin.demo", "correct-password"));

        verify(authenticationManager).authenticate(argThat(authentication ->
            authentication.getName().equals("medecin.demo") &&
            authentication.getCredentials().equals("correct-password")));
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().id()).isEqualTo(7L);
        assertThat(response.user().role()).isEqualTo(Role.MEDECIN);
        assertThat(user.getRefreshTokenHash()).isNotBlank().isNotEqualTo("refresh-token");
        assertThat(user.getRefreshTokenExpiresAt()).isNotNull();
    }

    @Test
    void refreshRejectsARevokedToken() {
        when(jwt.valid("revoked-token")).thenReturn(true);
        when(jwt.type("revoked-token")).thenReturn("refresh");
        when(jwt.username("revoked-token")).thenReturn("medecin.demo");
        when(users.findByUsernameAndActifTrue("medecin.demo")).thenReturn(Optional.of(user));
        user.setRefreshTokenHash("different-hash");

        assertThatThrownBy(() -> service.refresh(new RefreshRequest("revoked-token")))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("revoked");
    }

    @Test
    void logoutRevokesPersistedRefreshToken() {
        user.setRefreshTokenHash("stored-hash");
        when(users.findByUsernameAndActifTrue("medecin.demo")).thenReturn(Optional.of(user));

        service.logout("medecin.demo");

        assertThat(user.getRefreshTokenHash()).isNull();
        assertThat(user.getRefreshTokenExpiresAt()).isNull();
    }
}
