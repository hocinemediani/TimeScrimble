package com.narbaniki.timescrimble;
 
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
 
/** La classe AuthService permet de gérer de manière centralisée
 * les connexions et les enregistrements d'utilisateurs.<br>
 * Deux types de connexions sont possibles :<br>
 * - en tant qu'invité, sans mot de passe,<br>
 * - en tant qu'utilisateur, avec un mot de passe.<br><br>
 * Dans tous les cas, la clef principale de chaque utilisateur,
 * invité ou non, est son pseudonyme qui doit être unique.
 */
@Service
public class AuthService {

    /** Le répertoire des utilisateurs. */
    private final UtilisateurRepository utilisateurRepository;
    /** Le hasheur de mots de passe. */
    private final PasswordEncoder passwordEncoder;


    /** Créé une instance de AuthService.
     * @param utilisateurRepository Le répertoire des utilisateurs
     * @param passwordEncoder Le hasheur de mots de passe
     */
    public AuthService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }


    /** Crée un nouveau compte utilisateur et le sauvegarde dans la base.
     * @param pseudo Le pseudonyme de l'utilisateur à créer
     * @param motDePasse Le mot de passe (en clair) de l'utilisateur
     * @return Le nouvel utilisateur sauvegardé ou une exception si les<br>
     * données entrées ne sont pas conformes
     */
    public Utilisateur register(String pseudo, String motDePasse) throws IllegalArgumentException {
        if (utilisateurRepository.existsByUsername(pseudo)) {
            System.out.println("Ce pseudonyme est déjà utilisé.");
            throw new IllegalArgumentException();
        }
        if (pseudo.length() > 20 || pseudo.isEmpty()) {
            System.out.println("Le pseudonyme doit comprendre entre 1 et 20 caractères.");
            throw new IllegalArgumentException();
        }
        if (motDePasse.length() <= 1) {
            System.out.println("Le mot de passe doit contenir au moins un caractère.");
            throw new IllegalArgumentException();
        }
        Utilisateur newUtilisateur = new Utilisateur(pseudo, passwordEncoder.encode(motDePasse), false);
        /* Créé et assigne une clef API pour le nouvel utilisateur, utilisée pour récuperer ses informations. */
        newUtilisateur.setApiToken(UUID.randomUUID().toString());
        return utilisateurRepository.save(newUtilisateur);
    }


    /** Vérifie les identifiants entrés et retourne l'Utilisateur si corrects.
     * @param pseudo Le pseudonyme à vérifier
     * @param motDePasse Le mot de passe 
     * @return L'utilisateur si il existe un utilisateur pour le couple<br>
     * (pseudo, mot de passe) ou une exception sinon
     */
    public Utilisateur connect(String pseudo, String motDePasse) {
        Optional<Utilisateur> optUtilisateur = utilisateurRepository.findByUsername(pseudo);
        if (optUtilisateur.isEmpty()) {
            System.out.println("Utilisateur non trouvé dans la base de donnée.");
            throw new IllegalArgumentException();
        }
        Utilisateur utilisateur = optUtilisateur.get();
        if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            System.out.println("Pseudonyme ou mot de passe incorrect.");
            throw new IllegalArgumentException();
        }
        return utilisateur;
    }


    /** Vérifie les identifiants entrés et retourne l'Utilisateur guest si corrects.
     * @param pseudo Le pseudonyme du guest
     * @return L'utilisateur si ce dernier existe en base de données, ou une<br>
     * exception sinon
     */
    public Utilisateur connectGuest(String pseudo) {
        if (utilisateurRepository.existsByUsername(pseudo)) {
            System.out.println("Ce pseudonyme est déjà utilisé.");
            throw new IllegalArgumentException();
        }
        if (pseudo.length() > 20 || pseudo.isEmpty()) {
            System.out.println("Le pseudonyme doit contenir entre 1 et 20 caractères.");
            throw new IllegalArgumentException();
        }
        Utilisateur utilisateurGuest = new Utilisateur(pseudo, "guest", true);
        /* Créé et assigne une clef API pour le nouveau guest, utilisée pour récuperer ses informations. */
        utilisateurGuest.setApiToken(UUID.randomUUID().toString());
        return utilisateurRepository.save(utilisateurGuest);
    }
}
