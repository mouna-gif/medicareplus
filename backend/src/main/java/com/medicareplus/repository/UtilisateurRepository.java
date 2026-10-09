package com.medicareplus.repository;
import com.medicareplus.domain.Utilisateur;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByUsernameAndActifTrue(String username);
}
