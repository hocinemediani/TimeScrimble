package com.narbaniki.timescrimble;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer>{

    Optional<Utilisateur> findByPseudo(String pseudo);

    boolean existsByPseudo(String pseudo);
    
}
