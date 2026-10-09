package com.medicareplus.security;

import com.medicareplus.domain.Utilisateur;
import com.medicareplus.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UtilisateurRepository users;
    @Override
    public UserDetails loadUserByUsername(String username) {
        Utilisateur user = users.findByUsernameAndActifTrue(username)
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return User.withUsername(user.getUsername()).password(user.getPasswordHash())
            .roles(user.getRole().name()).disabled(!user.isActif()).build();
    }
}
