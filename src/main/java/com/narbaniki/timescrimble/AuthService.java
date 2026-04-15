package com.narbaniki.timescrimble;
 
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
 
/**
 * Gère l'inscription, la connexion et les joueurs invités.
 */
@Service
public class AuthService {
    
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }
 
 
    /**
     * Crée un nouveau compte et le sauvegarde en base.
     * @param pseudo
     * @param motDePasse 
     * @return le nouvel utilisateur
     */
    public Utilisateur register(String pseudo, String motDePasse) {
        if (utilisateurRepository.existsByUsername(pseudo)) {
            System.out.println("Ce pseudonyme est déjà utilisé.");
            throw new IllegalArgumentException("Ce pseudo est déjà utilisé.");
        }
        if (pseudo.length() > 20 || pseudo.isEmpty()) {
            System.out.println("Le pseudonyme doit comprendre entre 1 et 20 caractères.");
            throw new IllegalArgumentException("Le pseudo doit avoir au minimum un caractère et au maximum 20.");
        }
        if (motDePasse.length() <= 1 ) {
            System.out.println("Le mot de passe doit contenir au moin un caractère.");
            throw new IllegalArgumentException("Le mot de passe doit avoir au moins un caractère.");
        }
        Utilisateur newUtilisateur = new Utilisateur(pseudo, passwordEncoder.encode(motDePasse), false);
        newUtilisateur.setApiToken(UUID.randomUUID().toString());
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
        Optional<Utilisateur> optUtilisateur = utilisateurRepository.findByUsername(pseudo);
        if (optUtilisateur.isEmpty()) {
            System.out.println("Utilisateur non trouvé dans la base de donnée.");
            throw new IllegalArgumentException("Pseudo ou mot de passe invalide.");
        }
        Utilisateur utilisateur = optUtilisateur.get();
        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            System.out.println("Pseudonyme ou mot de passe incorrect.");
            throw new IllegalArgumentException("Pseudo ou mot de passe invalide.");
        }
        return utilisateur;
    }

    /**
     * Vérifie les identifiants du guest.
     *
     * @param pseudo
     * @return l'utilisateur 
     */
    public Utilisateur connectGuest(String pseudo) {
        if (utilisateurRepository.existsByUsername(pseudo)) {
            System.out.println("Ce pseudonyme est déjà utilisé.");
            throw new IllegalArgumentException("Ce pseudo est déjà utilisé.");
        }
        if (pseudo.length() > 20 || pseudo.isEmpty()) {
            System.out.println("Le pseudonyme doit contenir entre 1 et 20 caractères.");
            throw new IllegalArgumentException("Le pseudo doit avoir au minimum un caractère et au maximum 20.");
        }
        Utilisateur utilisateurGuest = new Utilisateur(pseudo, "guest", true);
        utilisateurGuest.setApiToken(UUID.randomUUID().toString());
        return utilisateurRepository.save(utilisateurGuest);
    }
}
