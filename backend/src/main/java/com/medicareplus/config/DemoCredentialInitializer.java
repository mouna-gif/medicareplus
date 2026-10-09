package com.medicareplus.config;

import com.medicareplus.repository.UtilisateurRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoCredentialInitializer implements CommandLineRunner {
    private static final List<String> DEMO_USERS = List.of("admin.demo", "medecin.demo", "secretaire.demo");
    private final UtilisateurRepository users;
    private final PasswordEncoder passwordEncoder;
    @Value("${DEMO_PASSWORD}")
    private String demoPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (demoPassword == null || demoPassword.length() < 12) {
            throw new IllegalStateException("DEMO_PASSWORD must contain at least 12 characters");
        }
        DEMO_USERS.forEach(username -> users.findByUsernameAndActifTrue(username)
            .ifPresent(user -> user.setPasswordHash(passwordEncoder.encode(demoPassword))));
    }
}
