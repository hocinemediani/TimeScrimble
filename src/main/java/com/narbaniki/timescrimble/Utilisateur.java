package com.narbaniki.timescrimble;

import java.io.Serializable;

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
public class Utilisateur implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Pseudo affiché en jeu. Doit être unique en base.
     */
    @NotBlank
    @Size(min = 1, max = 20)
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    /**
     * Mot de passe hashé.
     */
    @NotBlank
    @Column(nullable = false)
    private String motDePasse;

    /**
     * Token d'accès aléatoire pour l'API.
     */
    @Column(unique = true)
    private String apiToken;

    /**
     * Nombre total de parties jouées (toutes sessions confondues).
     * Incrémenté à la fin de chaque partie.
     */
    @Column(nullable = false)
    private int totalParties;

    /**
     * Cumul des points sur toutes les parties (pour le classement global).
     */
    @Column(nullable = false)
    private int victoires;

    // Constructeurs

    protected Utilisateur() {}

    public Utilisateur(String pseudo, String motDePasse) {
        this.username = pseudo;
        this.motDePasse = motDePasse;
        this.apiToken = "";
        this.totalParties = 0;
        this.victoires = 0;
    }


    /**
     * Ajoute les points d'une session terminée au cumul global.
     * Appeler une seule fois à la fin de chaque partie.
     */
    public void incrementerParties() {
        this.totalParties += 1;
    }

    public void incrementerVictoires() {
        this.victoires += 1;
    }

    // Getters / Setters

    public int getId() { 
        return id;
    }

    public String getPseudo() { 
        return username;
    }

    public void setPseudo(String pseudo) { 
        this.username = pseudo;
    }

    public String getMotDePasse() { 
        return motDePasse; 
    }

    public void setMotDePasse(String motDePasse) { 
        this.motDePasse = motDePasse;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public int getTotalParties() { 
        return totalParties; 
    }

    public int getVictoires() { 
        return victoires;
    }

}
