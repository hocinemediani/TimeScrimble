package com.narbaniki.timescrimble;
import java.util.List;

import org.springframework.stereotype.Service;

/** La classe LobbyService encapsule la logique métier liée à la gestion du lobby.<br>
 * Elle permet d'effectuer les opérations principales telles que :<br>
 * - La création d'une nouvelle partie,<br>
 * - L'ajout d'un joueur à une partie existante,<br>
 * - La récupération des parties publiques ou d'une partie spécifique.
 */
@Service
public class LobbyService {

    /** Le répertoire permettant l'accès aux données des parties en base de données. */
    private final PartieRepository partieRepository;


    /** Créé une instance de LobbyService.
     * @param partieRepository Le répertoire permettant l'accès aux données des parties
     */
    public LobbyService(PartieRepository partieRepository) {
        this.partieRepository = partieRepository;
    }


    /** Récupère et retourne la liste de toutes les parties publiques actuellement créées.
     * @return La liste des parties publiques (non privées)
     */
    public List<Partie> allPubliques(){
        return this.partieRepository.findAllByEstPriveeFalse();
    }


    /** Crée une nouvelle partie avec les paramètres spécifiés et y ajoute le créateur.<br>
     * Le nombre maximum de joueurs est automatiquement restreint entre 2 et 24.
     * @param nom Le nom de la partie à créer
     * @param nb Le nombre maximum de joueurs souhaité
     * @param priv Indique si la partie doit être privée (true) ou publique (false)
     * @param user L'utilisateur créant et rejoignant la partie
     * @return Le code unique généré pour la nouvelle partie
     */
    public String create(String nom, Integer nb, Boolean priv, Utilisateur user) {
        Partie partie = new Partie();
        partie.setNom(nom);
        int nbJoueursMax = Math.min(Math.max(nb, 2), 24);
        partie.setNbJoueursMax(nbJoueursMax);
        partie.setEstPrivee(priv);
        Joueur joueur = new Joueur(user, partie);
        partie.getJoueurs().add(joueur);
        partieRepository.save(partie);
        return partie.getCode();
    }


    /** Permet à un utilisateur de rejoindre une partie existante via son code.<br>
     * Vérifie au préalable que la partie existe et qu'elle n'est pas déjà pleine.
     * @param code Le code unique de la partie à rejoindre
     * @param user L'utilisateur souhaitant rejoindre la partie
     * @throws IllegalArgumentException Si aucune partie ne correspond au code fourni
     * @throws IllegalStateException Si la partie a déjà atteint son nombre maximum de joueurs
     */
    public void join(String code, Utilisateur user) {
        Partie partie = partieRepository.findByCode(code);
        if (partie == null) {
            System.out.printf("Aucune partie n'existe avec ce code : %s", code);
            throw new IllegalArgumentException("Partie non trouvée avec ce code.");
        }
        if (partie.getJoueurs().size() >= partie.getNbJoueursMax()) {
            System.out.println("La partie est déjà pleine.");
            throw new IllegalStateException("La partie est déjà pleine.");
        }    
        Joueur joueur = new Joueur(user, partie);
        partie.ajouterJoueur(joueur);
        partieRepository.save(partie);
    }


    /** Recherche et retourne une partie spécifique en fonction de son code.
     * @param code Le code unique de la partie recherchée
     * @return La partie correspondante, ou null si elle n'existe pas
     */
    public Partie getPartie(String code) {
        return partieRepository.findByCode(code);
    }
    
}
