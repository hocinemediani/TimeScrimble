package com.narbaniki.timescrimble;
 
import java.util.Optional;

import org.springframework.stereotype.Service;
 
/**
 * Gère l'inscription, la connexion et les joueurs invités.
 */
@Service
public class AuthService {
 
    private UtilisateurRepository utilisateurRepository;

    public AuthService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }
 
 
    /**
     * Crée un nouveau compte et le sauvegarde en base.
     * @param pseudo
     * @param motDePasse 
     * @return le nouvel utilisateur
     */
    public Utilisateur register(String pseudo, String motDePasse) {
 
        // Vérifie que le pseudo n'est pas déjà utilisé
        if (utilisateurRepository.existsByPseudo(pseudo)) {
            throw new IllegalArgumentException("Ce pseudo est déjà utilisé.");
        }
 
        Utilisateur newUtilisateur = new Utilisateur(pseudo, motDePasse);
        return utilisateurRepository.save(newUtilisateur);
    }
 
    /**
     * Vérifie les identifiants et retourne l'Utilisateur si corrects.
     *
     * @param pseudo
     * @param motDePasse
     * @return l'utilisateur 
     */
    public Utilisateur connect(String pseudo, String motDePasse) throws IllegalArgumentException {
 
        // Cherche l'utilisateur par pseudo
        Optional<Utilisateur> optUtilisateur = utilisateurRepository.findByPseudo(pseudo);
 
        if (optUtilisateur.isEmpty()) {
            throw new IllegalArgumentException("Pseudo ou mot de passe invalide.");
        }
 
        Utilisateur utilisateur = optUtilisateur.get();
 
        // Compare le mot de passe
        if (!utilisateur.getMotDePasse().equals(motDePasse)) {
            throw new IllegalArgumentException("Pseudo ou mot de passe invalide.");
        }
        return utilisateur;
    }
}
