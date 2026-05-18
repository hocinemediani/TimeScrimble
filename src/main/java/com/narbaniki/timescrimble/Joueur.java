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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** La classe Joueur représente un participant au sein d'une partie.<br>
 * Elle stocke les données éphémères relatives à la session de jeu telles que :<br>
 * - Le score actuel de la session,<br>
 * - L'état du joueur (dessinateur, a deviné, invité),<br>
 * - Le classement pour le devinage du mot.<br>
 * Elle fait le lien entre un Utilisateur persistant et une Partie.
 */
@Entity
@Table(name = "joueurs", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"partie_id", "utilisateur_id"})})
public class Joueur {

    /** L'identifiant unique du joueur dans la base de données. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Le pseudonyme du joueur, hérité de l'utilisateur. */
    @NotBlank
    @Size(min = 2, max = 30)
    @Column(nullable = false, length = 30)
    private String pseudo;

    /** Le score du joueur accumulé au cours de la partie. */
    @Column(nullable = false)
    private int scoreSession = 0;

    /** Le rang auquel le joueur a trouvé le mot. Utilisé pour calculer les points. */
    @Transient
    private int rangDevinage = 0;
 
    /** Vrai si le joueur est le dessinateur actuel de la manche, faux sinon. */
    @Column(nullable = false)
    private boolean estDessinateur = false;

    /** Vrai si le joueur joue en tant qu'invité, faux sinon. */
    @Column(nullable = false)
    private boolean estInvite = false;

    /** Vrai si le joueur a deviné le mot de la manche actuelle, faux sinon. */
    @Column(nullable = false)
    private boolean aDevine = false;

    /** Le compte utilisateur associé à ce joueur. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = true)
    private Utilisateur utilisateur;

    /** La partie dans laquelle le joueur se trouve. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partie_id", nullable = false)
    private Partie partie;
 

    /** Constructeur vide utilisé par JPA pour l'instanciation de l'entité. */
    protected Joueur() {}
 

    /** Créé une instance de Joueur à partir d'un utilisateur et d'une partie.<br>
     * Assigne le pseudonyme et le statut d'invité en se basant sur l'utilisateur.
     * @param utilisateur L'utilisateur rejoignant la partie
     * @param partie La partie rejointe
     */
    public Joueur(Utilisateur utilisateur, Partie partie) {
        this.utilisateur = utilisateur;
        this.pseudo = utilisateur.getPseudo();
        this.partie = partie;
        this.estInvite = utilisateur.getisInvite();
    }
 
 
    /** Ajoute des points au score de la session du joueur.<br>
     * Si la valeur fournie est négative, le score n'est pas modifié.
     * @param points Le nombre de points à ajouter
     */
    public void ajouterPoints(int points) {
        this.scoreSession = (points < 0) ? this.scoreSession : this.scoreSession + points;
    }


    /** Modifie le score de session du joueur pour la valeur {@code points}.
     * @param points Le nouveau score du joueur
     */
    public void setPoints(int points) {
        this.scoreSession = points;
    }


    /** Réinitialise les variables d'état du joueur pour préparer une nouvelle manche.<br>
     * L'état de devinage et le rôle de dessinateur repassent à faux.
     */
    public void reinitialiserPourNouvelleManche() {
        this.aDevine = false;
        this.estDessinateur = false;
    }


    /** Modifie le rang de devinage du joueur pour la valeur {@code rangDevinage}.
     * @param rangDevinage Le rang auquel le joueur a trouvé le mot
     */
    public void setRangDevinage(int rangDevinage) {
        this.rangDevinage = rangDevinage;
    }


    /** Retourne le rang de devinage du joueur.
     * @return Le rang auquel le joueur a trouvé le mot
     */
    public int getRangDevinage() {
        return rangDevinage;
    }


    /** Modifie le statut de réussite du joueur indiquant si le mot a été deviné.
     * @param aDevine Le nouvel état de réussite de devinage
     */
    public void setDevine(boolean aDevine) {
        this.aDevine = aDevine;
    }

    
    /** Définit le joueur comme étant le dessinateur pour la manche en cours. */
    public void designerCommeDessinateur() {
        this.estDessinateur = true;
    }


    /** Retourne l'identifiant du joueur.
     * @return L'identifiant unique du joueur
     */
    public int getId() { 
        return id; 
    }


    /** Retourne le pseudonyme du joueur.
     * @return Le pseudonyme du joueur
     */
    public String getPseudo() { 
        return pseudo;
    }


    /** Modifie le pseudonyme du joueur pour le pseudonyme {@code pseudo}.
     * @param pseudo Le pseudonyme à affecter au joueur
     */
    public void setPseudo(String pseudo) { 
        this.pseudo = pseudo; 
    }


    /** Retourne le score de la session du joueur.
     * @return Le score du joueur accumulé au cours de la partie
     */
    public int getScoreSession() { 
        return scoreSession; 
    }


    /** Indique si le joueur est le dessinateur.
     * @return Vrai si le joueur est dessinateur, faux sinon
     */
    public boolean isEstDessinateur() { 
        return estDessinateur; 
    }


    /** Modifie le rôle de dessinateur du joueur.
     * @param estDessinateur Vrai pour définir le joueur comme dessinateur, faux sinon
     */
    public void setEstDessinateur(boolean estDessinateur) {
         this.estDessinateur = estDessinateur; 
    }


    /** Indique si le joueur est un invité.
     * @return Vrai si le joueur est invité, faux sinon
     */
    public boolean isInvite() { 
        return estInvite; 
    }


    /** Indique si le joueur a deviné le mot lors de la manche actuelle.
     * @return Vrai si le joueur a deviné, faux sinon
     */
    public boolean isADevine() { 
        return aDevine; 
    }


    /** Retourne le compte utilisateur associé à ce joueur.
     * @return L'utilisateur rattaché au joueur
     */
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }


    /** Retourne la partie à laquelle le joueur participe.
     * @return La partie associée au joueur
     */
    @JsonIgnore
    public Partie getPartie() { 
        return partie;
    }


    /** Modifie la partie du joueur pour la partie {@code partie}.
     * @param partie La partie à affecter au joueur
     */
    public void setPartie(Partie partie) { 
        this.partie = partie;
    }
}
