package com.narbaniki.timescrimble;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * On crée un joueur à chaque fois qu'un utilisateur (qui peut être invité)
 * rejoint une partie. Il est différent de l'entité Utilisateur qui représente le compte
 * crée : un même Utilisateur créera un nouveau Joueur à chaque partie.
 *
 * invité : estInvite = true, utilisateur = null.
 * connecté : estInvite = false, utilisateur = l'Utilisateur.
 *
 * Relations :
 *   - Appartient à une Partie (ManyToOne).
 *   - Peut être lié à un Utilisateur (ManyToOne, nullable pour les invités).
 */
@Entity
@Table(name = "joueurs")
public class Joueur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Pseudo affiché en jeu pour ce joueur dans cette partie.
     * Copié depuis Utilisateur.pseudo ou saisi librement par l'invité.
     * On le dénormalise ici pour éviter une jointure sur chaque affichage.
     */
    @NotBlank
    @Size(min = 2, max = 30)
    @Column(nullable = false, length = 30)
    private String pseudo;

    /** Points accumulés pendant cette partie. Remis à 0 à la prochaine. */
    @Column(nullable = false)
    private int scoreSession = 0;
 
    /**
     * true si ce joueur est le dessinateur de la manche courante.
     * Mis à jour uniquement par PartieService lors de la rotation des rôles.
     * PartieService à faire
     */
    @Column(nullable = false)
    private boolean estDessinateur = false;
 
    /** true si ce joueur joue sans compte. Dans ce cas utilisateur = null. */
    @Column(nullable = false)
    private boolean estInvite = false;
 
    /** true si ce joueur a trouvé le mot de la manche courante. */
    @Column(nullable = false)
    private boolean aDevine = false;
 
    /**
     * Identifiant de la connexion WebSocket (SockJS sessionId).
     * Permet de cibler ce joueur pour un message direct (ex : lui révéler le mot).
     *
     * @Transient : pas stocké en base. Le socketId change à chaque reconnexion,
     * il n'a de sens que dans la mémoire du serveur en cours d'exécution.
     */
    private String socketId;
 
    // Relations
 
    /**
     * Compte de l'utilisateur lié à ce joueur. Null pour les invités.
     *
     * @ManyToOne : plusieurs Joueur peuvent pointer vers le même Utilisateur
     *   (un utilisateur joue plusieurs parties au fil du temps).
     *
     * FetchType.LAZY : Hibernate ne charge pas l'Utilisateur automatiquement.
     *   Il n'est chargé que si on appelle getUtilisateur(). Évite une jointure
     *   inutile sur utilisateurs à chaque fois qu'on lit un Joueur.
     *
     * nullable = true : autorisé à être null (cas des invités).
     */
    @JsonIgnore // Cache l'Utilisateur (évite d'exposer le mot de passe)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = true)
    private Utilisateur utilisateur;
 
    /**
     * Partie à laquelle ce joueur appartient. Jamais null.
     *
     * @ManyToOne : plusieurs Joueurs appartiennent à une même Partie.
     *
     * nullable = false : un Joueur existe toujours dans le contexte d'une Partie.
     */
    @JsonIgnore // Cache la référence vers Partie (évite la boucle infinie)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partie_id", nullable = false)
    private Partie partie;
 
    // Constructeurs
 
    protected Joueur() {}
 
    /**
     * Crée un joueur connecté avec compte Utilisateur.
     */
    public Joueur(Utilisateur utilisateur, Partie partie) {
        this.utilisateur = utilisateur;
        this.pseudo = utilisateur.getPseudo();
        this.partie = partie;
        this.estInvite = false;
    }
 
    /**
     * Crée un joueur invité (sans compte).
     * Le pseudo est saisi librement.
     */
    public Joueur(String pseudo, Partie partie) {
        this.pseudo = pseudo;
        this.utilisateur = null;
        this.partie = partie;
        this.estInvite = true;
    }
 
 
    /**
     * Ajoute des points au score de session de ce joueur.
     * Si le joueur a un compte, met aussi à jour le cumul global sur l'Utilisateur.
     *
     * @param points doit être positif ou nul
     */
    public void ajouterPoints(int points) {
        if (points < 0) throw new IllegalArgumentException("Les points ne peuvent pas être négatifs.");
        this.scoreSession += points;
    }
 
    /**
     * Réinitialise l'état entre deux manches.
     * scoreSession est conservé (cumulatif sur toute la partie).
     */
    public void reinitialiserPourNouvelleManche() {
        this.aDevine = false;
        this.estDessinateur = false;
    }
 
    /** Marque ce joueur comme ayant deviné le mot de la manche courante. */
    public void marquerCommeDevine() {
        this.aDevine = true;
    }
 
    /** Désigne ce joueur comme dessinateur pour la manche courante. */
    public void designerCommeDessinateur() {
        this.estDessinateur = true;
    }

    // Getters / Setters

    public Long getId() { 
        return id; 
    }

    public String getPseudo() { 
        return pseudo;
    }
    public void setPseudo(String pseudo) { 
        this.pseudo = pseudo; 
    }

    public int getScoreSession() { 
        return scoreSession; 
    }

    public boolean isEstDessinateur() { 
        return estDessinateur; 
    }
    public void setEstDessinateur(boolean estDessinateur) {
         this.estDessinateur = estDessinateur; 
        }

    public boolean isEstInvite() { 
        return estInvite; 
    }

    public boolean isADevine() { 
        return aDevine; 
    }

    public String getSocketId() { 
        return socketId; 
    }
    public void setSocketId(String socketId) { 
        this.socketId = socketId;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public Partie getPartie() { 
        return partie;
    }
    public void setPartie(Partie partie) { 
        this.partie = partie;
    }
}
