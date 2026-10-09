package com.medicareplus.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.medicareplus.domain.Enums.Role;
import com.medicareplus.domain.Utilisateur;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private final JwtService jwt = new JwtService(
        "test-secret-with-at-least-thirty-two-characters",
        900_000,
        604_800_000
    );

    @Test
    void issuesDistinctTypedTokensWithDatabaseUserIdentity() {
        Utilisateur user = new Utilisateur();
        user.setUsername("admin.demo");
        user.setRole(Role.ADMIN);

        String firstAccess = jwt.accessToken(user);
        String secondAccess = jwt.accessToken(user);
        String refresh = jwt.refreshToken(user);

        assertThat(firstAccess).isNotEqualTo(secondAccess);
        assertThat(jwt.valid(firstAccess)).isTrue();
        assertThat(jwt.username(firstAccess)).isEqualTo("admin.demo");
        assertThat(jwt.type(firstAccess)).isEqualTo("access");
        assertThat(jwt.type(refresh)).isEqualTo("refresh");
    }
}
