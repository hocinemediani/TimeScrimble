package com.narbaniki.timescrimble;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Représente un compte joueur.
 *
 * Un Utilisateur peut participer à plusieurs parties en tant que Joueur.
 * Les invités (sans compte) n'ont pas d'Utilisateur associé.
 */
@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Pseudo affiché en jeu. Doit être unique en base.
     */
    @NotBlank
    @Size(min = 2, max = 30)
    @Column(nullable = false, unique = true, length = 30)
    private String pseudo;

    /**
     * Mot de passe hashé.
     */
    @NotBlank
    @Column(nullable = false)
    private String motDePasse;

    /**
     * Nombre total de parties jouées (toutes sessions confondues).
     * Incrémenté à la fin de chaque partie.
     */
    @Column(nullable = false)
    private int totalParties = 0;

    /**
     * Cumul des points sur toutes les parties (pour le classement global).
     */
    @Column(nullable = false)
    private int totalPoints = 0;

    // Constructeurs

    protected Utilisateur() {}

    public Utilisateur(String pseudo, String motDePasse) {
        this.pseudo = pseudo;
        this.motDePasse = motDePasse;
    }


    /**
     * Ajoute les points d'une session terminée au cumul global.
     * Appeler une seule fois à la fin de chaque partie.
     */
    public void ajouterPoints(int points) {
        this.totalPoints += points;
        this.totalParties += 1;
    }

    // Getters / Setters

    public int getId() { 
        return id;
    }

    public String getPseudo() { 
        return pseudo;
    }

    public void setPseudo(String pseudo) { 
        this.pseudo = pseudo;
    }

    public String getMotDePasse() { 
        return motDePasse; 
    }

    public void setMotDePasse(String motDePasse) { 
        this.motDePasse = motDePasse;
    }

    public int getTotalParties() { 
        return totalParties; 
    }

    public int getTotalPoints() { 
        return totalPoints;
    }

}
