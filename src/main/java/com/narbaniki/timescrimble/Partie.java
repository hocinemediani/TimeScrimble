package com.narbaniki.timescrimble;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** La classe Partie représente une session de jeu (ou salle/room).<br>
 * Elle regroupe toutes les informations et la logique nécessaires au bon déroulement<br>
 * du jeu, notamment :<br>
 * - Les paramètres de la salle (nom, code d'accès, confidentialité, capacité),<br>
 * - L'état actuel d'avancement (statut de la partie, nombre de manches jouées),<br>
 * - La liste des joueurs présents et la gestion des tours (dessinateur actuel, devineurs).
 */
@Entity
@Table(name = "parties")
public class Partie {

    /** L'identifiant unique de la partie dans la base de données. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Le nom d'affichage de la partie. */
    @NotBlank
    @Size(min = 1, max = 50)
    @Column(nullable = false, length = 50)
    private String nom;

    /** Le code d'accès unique (généré aléatoirement) pour rejoindre la partie. */
    @Column(nullable = false, unique = true, length = 8)
    private String code;

    /** Indique si la partie est privée (nécessite le code) ou publique (visible dans le lobby). */
    @Column(nullable = false)
    private boolean estPrivee;

    /** Le nombre maximum de joueurs autorisés à rejoindre cette partie. */
    @Min(2)
    @Max(24)
    @Column(nullable = false)
    private int nbJoueursMax;

    /** Le statut actuel de la partie (en attente, en cours, terminée). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPartie statut = StatutPartie.ATTENTE;

    /** La date et l'heure de création de la partie. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /** La liste des joueurs actuellement dans la partie. */
    @OneToMany(mappedBy = "partie", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private final List<Joueur> joueurs = new ArrayList<>();

    /** Le mot que les joueurs doivent actuellement deviner lors de la manche. */
    @Column(nullable=true)
    private String motADeviner = "";

    /** L'index dans la liste des joueurs déterminant le dessinateur actuel. */
    private int indexDessinateurActuel = -1;

    /** Le nombre de joueurs ayant réussi à deviner le mot lors de la manche en cours. */
    private int ontDevine = 0;

    /** Le nombre total de manches ayant déjà été jouées dans cette partie. */
    private int manchesJouees = 0;


    /** Méthode exécutée automatiquement avant l'insertion en base de données.<br>
     * Elle initialise la date de création et génère un code d'accès unique à 6 caractères.
     */
    @PrePersist
    @SuppressWarnings("unused")
    private void preCreation() {
        this.dateCreation = LocalDateTime.now();
        if (this.code == null) {
            this.code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 6)
                    .toUpperCase();
        }
    }


    /** Constructeur vide utilisé par JPA pour l'instanciation de l'entité. */
    protected Partie() {}


    /** Créé une instance de Partie avec les paramètres spécifiés.
     * @param nom Le nom d'affichage de la partie
     * @param estPrivee Vrai si la partie est privée, faux si elle est publique
     * @param nbJoueursMax Le nombre maximum de joueurs autorisés
     */
    public Partie(String nom, boolean estPrivee, int nbJoueursMax) {
        this.nom = nom;
        this.estPrivee = estPrivee;
        this.nbJoueursMax = nbJoueursMax;
    }


    /** Prépare la partie pour démarrer une nouvelle manche.<br>
     * Cette méthode incrémente le compteur de manches, passe au dessinateur suivant,<br>
     * et réinitialise l'état de devinage pour tous les joueurs.
     */
    public void preparerNouvelleManche() {
        indexDessinateurActuel = (indexDessinateurActuel + 1) % joueurs.size();
        manchesJouees++;
        for (Joueur j : joueurs) {
            j.setEstDessinateur(false);
            j.setDevine(false);
            j.setRangDevinage(0);
        }
        Joueur dessinateur = joueurs.get(indexDessinateurActuel);
        dessinateur.setEstDessinateur(true);
        resetOntDevine();
    }


    /** Vérifie si la manche en cours est terminée.<br>
     * Une manche est considérée comme terminée si tous les joueurs (à l'exception du dessinateur)<br>
     * ont réussi à deviner le mot.
     * @return Vrai si la manche est terminée, faux sinon
     */
    public boolean checkFinManche() {
        for (Joueur joueur : joueurs) {
            if (!joueur.isEstDessinateur() && !joueur.isADevine()) {
                return false;
            }
        }
        return true;
    }


    /** Lance le démarrage de la partie.<br>
     * Modifie le statut de la partie si les conditions (minimum de joueurs) sont respectées.
     * @throws IllegalStateException Si le nombre de joueurs est insuffisant ou si la partie n'est plus en attente
     */
    public void demarrer() throws IllegalStateException {
        if (joueurs.size() < 2) {
            throw new IllegalStateException("Il faut au moins 2 joueurs pour lancer la partie.");
        }
        if (statut != StatutPartie.ATTENTE) {
            throw new IllegalStateException("La partie est déjà lancée ou terminée.");
        }
        this.statut = StatutPartie.EN_COURS;
    }


    /** Force la fin de la partie en modifiant son statut.
     * @throws IllegalStateException Si la partie n'est pas actuellement en cours
     */
    public void terminer() throws IllegalStateException {
        if (statut != StatutPartie.EN_COURS) {
            throw new IllegalStateException("Seule une partie en cours peut être terminée.");
        }
        this.statut = StatutPartie.TERMINEE;
    }


    /** Ajoute un nouveau joueur à la partie.<br>
     * Vérifie que la partie est toujours en attente et qu'elle n'est pas pleine.
     * @param joueur Le joueur à ajouter
     * @throws IllegalStateException Si la partie a déjà commencé ou si elle est pleine
     */
    public void ajouterJoueur(Joueur joueur) throws IllegalStateException {
        if (statut != StatutPartie.ATTENTE) {
            throw new IllegalStateException("Impossible de rejoindre une partie déjà lancée ou terminée.");
        }
        if (joueurs.size() >= nbJoueursMax) {
            throw new IllegalStateException("La partie est complète (" + nbJoueursMax + "/" + nbJoueursMax + ").");
        }
        joueur.setPartie(this);
        joueurs.add(joueur);
    }


    /** Retire un joueur de la partie et supprime son association.
     * @param joueur Le joueur à retirer
     */
    public void retirerJoueur(Joueur joueur) {
        joueur.setPartie(null);
        joueurs.remove(joueur);
    }


    /** Retourne le joueur hôte de la partie (le créateur/premier joueur à avoir rejoint).
     * @return Le joueur désigné comme hôte
     * @throws IllegalStateException Si la partie ne contient aucun joueur
     */
    @JsonIgnore
    public Joueur getHost() throws IllegalStateException {
        if (joueurs.isEmpty()) {
            throw new IllegalStateException("La partie ne contient aucun joueur.");
        }
        return joueurs.get(0);
    }


    /** Génère et retourne le classement actuel des joueurs en fonction de leurs scores.
     * @return Une liste contenant les pseudonymes des joueurs triés par score décroissant
     */
    @JsonIgnore
    public List<String> getLeaderboard() {
        return joueurs.stream()
            .sorted(Comparator.comparingInt(Joueur::getScoreSession).reversed())
            .map(Joueur::getPseudo)
            .toList();
    }


    /** Recherche et retourne le joueur qui est défini comme le dessinateur actuel.
     * @return Le joueur dessinateur, ou null s'il n'y en a pas
     */
    @JsonIgnore
    @Transient
    public Joueur getDessinateurActuel() {
        return joueurs.stream()
            .filter(Joueur::isEstDessinateur)
            .findFirst()
            .orElse(null);
    }


    /** Incrémente le compteur du nombre de joueurs ayant deviné le mot pour la manche. */
    public void incOntDevine() {
        ontDevine++;
    }


    /** Retourne le nombre de joueurs ayant deviné le mot lors de cette manche.
     * @return Le nombre de joueurs ayant deviné le mot
     */
    public int getOntDevine() {
        return ontDevine;
    }


    /** Réinitialise le compteur de joueurs ayant deviné le mot à zéro. */
    public void resetOntDevine() {
        ontDevine = 0;
    }


    /** Retourne le nombre total de manches ayant été jouées jusqu'à présent.
     * @return Le nombre de manches jouées
     */
    public int getManchesJouees() {
        return manchesJouees;
    }


    /** Indique si la partie a atteint son nombre maximum de joueurs autorisés.
     * @return Vrai si la partie est pleine, faux sinon
     */
    public boolean estPleine() {
        return joueurs.size() >= nbJoueursMax;
    }


    /** Retourne l'identifiant de la partie.
     * @return L'identifiant unique de la partie
     */
    public int getId() {
         return id;
    }


    /** Retourne le nom de la partie.
     * @return Le nom d'affichage de la partie
     */
    public String getNom() {
         return nom;
    }


    /** Modifie le nom de la partie pour la valeur {@code nom}.
     * @param nom Le nom à affecter à la partie
     */
    public void setNom(String nom) { 
        this.nom = nom;
    }


    /** Retourne le code d'accès de la partie.
     * @return Le code unique de la partie
     */
    public String getCode() {
         return code;
    }


    /** Indique si la partie est paramétrée comme privée.
     * @return Vrai si la partie est privée, faux sinon
     */
    public boolean isEstPrivee() { 
        return estPrivee; 
    }


    /** Modifie la confidentialité de la partie.
     * @param estPrivee Vrai pour définir la partie comme privée, faux pour publique
     */
    public void setEstPrivee(boolean estPrivee) { 
        this.estPrivee = estPrivee;
    }


    /** Retourne le nombre maximum de joueurs autorisés.
     * @return Le nombre maximum de joueurs
     */
    public int getNbJoueursMax() { 
        return nbJoueursMax;
    }


    /** Modifie le nombre maximum de joueurs autorisés pour la partie.
     * @param nbJoueursMax Le nombre maximum de joueurs à affecter
     */
    public void setNbJoueursMax(int nbJoueursMax) {
        this.nbJoueursMax = nbJoueursMax;
    }


    /** Retourne le statut actuel de la partie.
     * @return Le statut de la partie
     */
    public StatutPartie getStatut() {
        return statut;
    }


    /** Retourne la date et l'heure à laquelle la partie a été créée.
     * @return La date de création de la partie
     */
    public LocalDateTime getDateCreation() { 
        return dateCreation;
    }


    /** Retourne la liste des joueurs actuellement connectés à la partie.
     * @return La liste des joueurs de la partie
     */
    public List<Joueur> getJoueurs() {
        return joueurs; 
    }


    /** Retourne le mot que les joueurs doivent deviner lors de la manche actuelle.
     * @return Le mot à deviner
     */
    public String getMotADeviner() {
        return motADeviner;
    }


    /** Modifie le mot à deviner pour la manche actuelle.
     * @param motADeviner Le nouveau mot à faire deviner
     */
    public void setMotADeviner(String motADeviner) {
        this.motADeviner = motADeviner;
    }
}
