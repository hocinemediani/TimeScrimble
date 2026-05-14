package com.narbaniki.timescrimble;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JoueurRepository extends JpaRepository<Joueur, Integer>{
    public Optional<Joueur> findByPseudo(String pseudo);
}
