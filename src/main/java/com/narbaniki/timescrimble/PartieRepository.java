package com.narbaniki.timescrimble;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

/** L'interface PartieRepository permet de gérer l'accès aux données des entités Partie<br>
 * dans la base de données.<br>
 * Elle étend JpaRepository afin de bénéficier des opérations de base (CRUD) et<br>
 * déclare des méthodes de recherche et de suppression personnalisées spécifiques aux parties.
 */
public interface PartieRepository extends JpaRepository<Partie, Integer> {

    /** Recherche et retourne la liste de toutes les parties qui ne sont pas privées (publiques).
     * @return Une ArrayList contenant toutes les parties publiques disponibles
     */
    ArrayList<Partie> findAllByEstPriveeFalse();


    /** Recherche et retourne une partie spécifique en utilisant son code d'accès unique.
     * @param code Le code d'accès de la partie à rechercher
     * @return La partie correspondante, ou null si aucune partie ne possède ce code
     */
    Partie findByCode(String code);


    /** Supprime une partie de la base de données en fonction de son code d'accès unique.
     * @param code Le code d'accès de la partie à supprimer
     */
    void deletePartieByCode(String code);
}
