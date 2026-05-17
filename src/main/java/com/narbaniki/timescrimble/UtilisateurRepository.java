package com.narbaniki.timescrimble;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** L'interface UtilisateurRepository permet de gérer l'accès aux données des entités Utilisateur<br>
 * dans la base de données.<br>
 * Elle étend JpaRepository afin de bénéficier des opérations de base (CRUD) et<br>
 * déclare des méthodes de recherche et de vérification personnalisées spécifiques aux comptes.
 */
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {

    /** Recherche et retourne un utilisateur en fonction de son pseudonyme.
     * @param pseudo Le pseudonyme de l'utilisateur à rechercher
     * @return Un Optional contenant l'Utilisateur s'il existe, ou un Optional vide sinon
     */
    Optional<Utilisateur> findByUsername(String pseudo);

    /** Vérifie si un utilisateur possède déjà le pseudonyme spécifié dans la base de données.
     * @param pseudo Le pseudonyme à vérifier
     * @return Vrai si un compte utilise déjà ce pseudonyme, faux sinon
     */
    boolean existsByUsername(String pseudo);
    
    /** Recherche et retourne un utilisateur en se basant sur son token API (clef de session).
     * @param apiToken Le token API unique de l'utilisateur à rechercher
     * @return Un Optional contenant l'Utilisateur s'il existe, ou un Optional vide sinon
     */
    Optional<Utilisateur> findByApiToken(String apiToken);
}
