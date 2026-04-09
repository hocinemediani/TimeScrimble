package com.narbaniki.timescrimble;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer>{
    Optional<Utilisateur> findByUsername(String pseudo);
    boolean existsByUsername(String pseudo);
    Optional<Utilisateur> findByApiToken(String apiToken);
}
