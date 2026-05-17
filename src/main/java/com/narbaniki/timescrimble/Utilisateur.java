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

/** La classe Utilisateur représente un compte (enregistré ou invité) dans la base de données.<br>
 * Elle stocke les informations de connexion et les statistiques globales du joueur,<br>
 * telles que :<br>
 * - Le pseudonyme et le mot de passe (haché),<br>
 * - Le token API utilisé pour l'authentification des requêtes session,<br>
 * - Le nombre total de parties jouées et de victoires accumulées,<br>
 * - Un indicateur précisant s'il s'agit d'un compte invité (temporaire).
 */
@Entity
@Table(name = "utilisateurs")
public class Utilisateur implements Serializable {

    /** L'identifiant unique de l'utilisateur dans la base de données. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Le pseudonyme unique de l'utilisateur, servant d'identifiant public et de connexion. */
    @NotBlank
    @Size(min = 1, max = 20)
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    /** Le mot de passe de l'utilisateur, stocké sous forme hachée pour la sécurité. */
    @NotBlank
    @Column(nullable = false)
    private String motDePasse;

    /** Le token d'authentification généré lors de la connexion, servant de clef de session. */
    @Column(unique = true)
    private String apiToken;

    /** Le nombre total de parties auxquelles l'utilisateur a participé jusqu'à la fin. */
    @Column(nullable = false)
    private int totalParties;

    /** Le nombre total de parties remportées (terminées à la première place) par l'utilisateur. */
    @Column(nullable = false)
    private int victoires;

    /** Indique si le compte a été créé en tant qu'invité (sans inscription formelle). */
    @Column(nullable = false)
    private Boolean isInvite;


    /** Constructeur vide utilisé par JPA pour l'instanciation de l'entité. */
    protected Utilisateur() {}


    /** Créé une instance d'Utilisateur avec les informations de base fournies.<br>
     * Initialise par défaut les statistiques de jeu (parties et victoires) à zéro<br>
     * et le token API à une chaîne vide.
     * @param pseudo Le pseudonyme choisi par l'utilisateur
     * @param motDePasse Le mot de passe (généralement haché en amont) associé au compte
     * @param isInvite Vrai si le compte est un compte invité, faux pour un compte standard
     */
    public Utilisateur(String pseudo, String motDePasse, Boolean isInvite) {
        this.username = pseudo;
        this.motDePasse = motDePasse;
        this.apiToken = "";
        this.totalParties = 0;
        this.victoires = 0;
        this.isInvite = isInvite;
    }


    /** Incrémente de un le nombre total de parties terminées par l'utilisateur. */
    public void incrementerParties() {
        this.totalParties += 1;
    }


    /** Incrémente de un le nombre total de victoires de l'utilisateur. */
    public void incrementerVictoires() {
        this.victoires += 1;
    }


    /** Retourne l'identifiant unique de l'utilisateur.
     * @return L'identifiant de l'utilisateur en base de données
     */
    public int getId() { 
        return id;
    }


    /** Retourne le pseudonyme de l'utilisateur.
     * @return Le pseudonyme de l'utilisateur
     */
    public String getPseudo() { 
        return username;
    }


    /** Modifie le pseudonyme de l'utilisateur pour la valeur {@code pseudo}.
     * @param pseudo Le nouveau pseudonyme à affecter à l'utilisateur
     */
    public void setPseudo(String pseudo) { 
        this.username = pseudo;
    }


    /** Retourne le mot de passe (haché) de l'utilisateur.
     * @return Le mot de passe de l'utilisateur
     */
    public String getMotDePasse() { 
        return motDePasse; 
    }


    /** Modifie le mot de passe de l'utilisateur pour la valeur {@code motDePasse}.
     * @param motDePasse Le nouveau mot de passe (haché) à affecter
     */
    public void setMotDePasse(String motDePasse) { 
        this.motDePasse = motDePasse;
    }


    /** Retourne le token API (clef de session) de l'utilisateur.
     * @return Le token API actuel de l'utilisateur
     */
    public String getApiToken() {
        return apiToken;
    }


    /** Modifie le token API de l'utilisateur pour la valeur {@code apiToken}.
     * @param apiToken Le nouveau token API à affecter à l'utilisateur
     */
    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }


    /** Retourne le nombre total de parties jouées par l'utilisateur.
     * @return Le nombre de parties jouées
     */
    public int getTotalParties() { 
        return totalParties; 
    }


    /** Retourne le nombre total de victoires remportées par l'utilisateur.
     * @return Le nombre de victoires
     */
    public int getVictoires() { 
        return victoires;
    }


    /** Indique si l'utilisateur utilise actuellement un compte invité.
     * @return Vrai si le compte est un compte invité, faux sinon
     */
    public Boolean getisInvite() { 
        return isInvite;
    }
}
