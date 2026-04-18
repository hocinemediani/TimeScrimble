package com.narbaniki.timescrimble;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


/**
 * Représente une salle de jeu et les règles associées à une partie.
 *
 * Etat d'une partie :
 *   ATTENTE → EN_COURS → TERMINEE
 *
 * Une partie est identifiée par un code unique à 6 caractères que les joueurs
 * utilisent pour rejoindre une partie privée. Les parties publiques sont listées
 * dans le lobby.
 *
 * Relations :
 *   - Contient 2 à nbJoueursMax Joueur(s).
 *   - Comporte 1 à n Manche(s) (référence unidirectionnelle, Manche non implémenté ici).
 */
@Entity
@Table(name = "parties")
public class Partie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Nom de la salle affiché dans le menu principal.
     */
    @NotBlank
    @Size(min = 1, max = 50)
    @Column(nullable = false, length = 50)
    private String nom;

    /**
     * Code de 6 caractères utilisé pour rejoindre
     * une partie privée. Généré automatiquement à la création.
     */
    @Column(nullable = false, unique = true, length = 8)
    private String code;

    /**
     * true  = visible uniquement via code (mode privé).
     * false = apparaît dans la liste des parties publiques.
     */
    @Column(nullable = false)
    private boolean estPrivee;

    /**
     * Nombre maximum de joueurs autorisés (entre 2 et 8).
     */
    @Min(2)
    @Max(8)
    @Column(nullable = false)
    private int nbJoueursMax;

    /**
     * Statut courant de la partie (ATTENTE, EN_COURS, TERMINEE).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPartie statut = StatutPartie.ATTENTE;

    /**
     * Date/heure de création de la salle (pour trier les parties publiques).
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /**
     * Liste des joueurs présents dans la salle.
     * CascadeType.ALL : si la partie est supprimée, les joueurs le sont aussi.
     * orphanRemoval : retire un Joueur de la base s'il quitte la partie.
     */
    @OneToMany(mappedBy = "partie", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Joueur> joueurs = new ArrayList<>();

    /**
     * Mot à deviner
     */
    @Column(nullable=false)
    private String motADeviner;

    // Hooks JPA

    @PrePersist
    private void preCreation() {
        this.dateCreation = LocalDateTime.now();
        if (this.code == null) {
            // Génère un code court de 6 caractères en majuscules
            this.code = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 6)
                    .toUpperCase();
        }
    }

    // Constructeurs

    protected Partie() {}

    public Partie(String nom, boolean estPrivee, int nbJoueursMax) {
        this.nom = nom;
        this.estPrivee = estPrivee;
        this.nbJoueursMax = nbJoueursMax;
    }

    /**
     * Ajoute un joueur à la partie.
     * Vérifie que la partie est en attente et qu'il reste de la place.
     *
     * @throws IllegalStateException si la partie est pleine ou déjà lancée.
     */
    public void ajouterJoueur(Joueur joueur) {
        if (statut != StatutPartie.ATTENTE) {
            throw new IllegalStateException("Impossible de rejoindre une partie déjà lancée ou terminée.");
        }
        if (joueurs.size() >= nbJoueursMax) {
            throw new IllegalStateException("La partie est complète (" + nbJoueursMax + "/" + nbJoueursMax + ").");
        }
        joueur.setPartie(this);
        joueurs.add(joueur);
    }

    /**
     * Retire un joueur de la partie.
     * Si la partie est en ATTENTE et qu'il ne reste aucun joueur, la partie peut
     * être supprimée par PartieService (PartieService à faire).
     */
    public void retirerJoueur(Joueur joueur) {
        joueur.setPartie(null);
        joueurs.remove(joueur);
    }

    /**
     * Passe la partie à l'état EN_COURS.
     * Seul le host (premier joueur) peut déclencher cela via PartieService.
     *
     * @throws IllegalStateException si moins de 2 joueurs sont présents.
     */
    public void demarrer() {
        if (joueurs.size() < 2) {
            throw new IllegalStateException("Il faut au moins 2 joueurs pour lancer la partie.");
        }
        if (statut != StatutPartie.ATTENTE) {
            throw new IllegalStateException("La partie est déjà lancée ou terminée.");
        }
        this.statut = StatutPartie.EN_COURS;
    }

    /**
     * Passe la partie à l'état TERMINEE.
     * Appelé par PartieService une fois la dernière manche résolue.
     */
    public void terminer() {
        if (statut != StatutPartie.EN_COURS) {
            throw new IllegalStateException("Seule une partie en cours peut être terminée.");
        }
        this.statut = StatutPartie.TERMINEE;
    }

    /**
     * Retourne le joueur host (créateur de la salle).
     * Par convention, c'est le premier joueur de la liste.
     *
     * @throws IllegalStateException si la partie est vide.
     */
    public Joueur getHost() {
        if (joueurs.isEmpty()) {
            throw new IllegalStateException("La partie ne contient aucun joueur.");
        }
        return joueurs.get(0);
    }

    /**
     * Indique s'il reste au moins une place disponible.
     */
    public boolean estPleine() {
        return joueurs.size() >= nbJoueursMax;
    }

    // Getters / Setters

    public int getId() {
         return id;
    }

    public String getNom() {
         return nom;
    }

    public void setNom(String nom) { 
        this.nom = nom;
    }

    public String getCode() {
         return code;
    }

    public boolean isEstPrivee() { 
        return estPrivee; 
    }

    public void setEstPrivee(boolean estPrivee) { 
        this.estPrivee = estPrivee;
    }

    public int getNbJoueursMax() { 
        return nbJoueursMax;
    }

    public void setNbJoueursMax(int nbJoueursMax) {
        this.nbJoueursMax = nbJoueursMax;
    }


    public StatutPartie getStatut() {
        return statut;
    }

    public LocalDateTime getDateCreation() { 
        return dateCreation;
    }

    public List<Joueur> getJoueurs() {
        return joueurs; 
    }

    public String getMotADeviner() {
        return motADeviner;
    }

    public void setMotADeviner(String motADeviner) {
        this.motADeviner = motADeviner;
    }
}
