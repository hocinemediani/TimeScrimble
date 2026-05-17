package com.narbaniki.timescrimble;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** L'interface JoueurRepository permet de gérer l'accès aux données des entités Joueur<br>
 * dans la base de données.<br>
 * Elle étend JpaRepository afin d'hériter des méthodes de base (CRUD) et<br>
 * déclare les méthodes de recherche personnalisées spécifiques aux joueurs.
 */
public interface JoueurRepository extends JpaRepository<Joueur, Integer> {
    
    /** Recherche et retourne un joueur en fonction de son pseudonyme.
     * @param pseudo Le pseudonyme du joueur à rechercher dans la base
     * @return Un Optional contenant le Joueur s'il existe, ou un Optional vide sinon
     */
    public Optional<Joueur> findByPseudo(String pseudo);
}
