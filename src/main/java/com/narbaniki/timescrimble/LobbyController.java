package com.narbaniki.timescrimble;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** La classe LobbyController est un contrôleur REST gérant les actions du lobby.<br>
 * Elle expose les endpoints permettant aux utilisateurs de :<br>
 * - Créer une nouvelle partie avec des paramètres spécifiques,<br>
 * - Rejoindre une partie existante à l'aide de son code unique,<br>
 * - Récupérer la liste de certaines parties disponibles.
 */
@RestController
@RequestMapping("/")
public class LobbyController {

    /** Le répertoire des utilisateurs permettant l'accès aux données en base. */
    @Autowired
    private UtilisateurRepository userRepository;

    /** Le service encapsulant la logique métier du lobby. */
    @Autowired
    private LobbyService lobbyService;


    /** Permet de créer une nouvelle partie avec les paramètres spécifiés.<br>
     * L'utilisateur créateur est identifié grâce à la clef API de sa session.
     * @param request La requête HTTP envoyée par le client
     * @param response La réponse HTTP associée
     * @param nom Le nom souhaité pour la nouvelle partie
     * @param nb Le nombre maximum de joueurs autorisés dans la partie
     * @param priv Un booléen indiquant si la partie est privée (true) ou publique (false)
     * @param session La session HTTP actuelle de l'utilisateur
     * @return Une ResponseEntity contenant le code de la partie créée en cas de succès,<br>
     * ou une erreur si l'utilisateur est introuvable
     */
    @PostMapping("/create")
    public ResponseEntity<String> createRoom(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam(value = "nom", required = true) String nom,
                            @RequestParam(value = "nbMax", required = true) Integer nb,
                            @RequestParam(value = "isPrivate", required = true) Boolean priv,
                            HttpSession session) {
        Optional<Utilisateur> userOpt = userRepository.findByApiToken((String) session.getAttribute("apiToken"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Couldn't find the user.");
        }
        Utilisateur user = userOpt.get();
        String code = lobbyService.create(nom, nb, priv, user);
        return ResponseEntity.ok(code);
    }


    /** Permet à un utilisateur de rejoindre une partie existante en utilisant son code.<br>
     * L'utilisateur rejoignant la partie est identifié grâce à la clef API de sa session.
     * @param request La requête HTTP envoyée par le client
     * @param response La réponse HTTP associée
     * @param code Le code unique de la partie à rejoindre
     * @param session La session HTTP actuelle de l'utilisateur
     * @return Une ResponseEntity contenant le code de la partie rejointe en cas de succès,<br>
     * ou une erreur si l'utilisateur est introuvable
     */
    @PostMapping("/join")
    public ResponseEntity<String> joinRoom(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam String code, HttpSession session) {
        Optional<Utilisateur> userOpt = userRepository.findByApiToken((String) session.getAttribute("apiToken"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Couldn't find the user.");
        }
        Utilisateur user = userOpt.get();
        try {
            lobbyService.join(code, user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Couldn't find the user.");
        }
        return ResponseEntity.ok(code); 
    }

    
    /** Retourne la liste des parties selon la logique du service.
     * @return La liste des parties correspondantes
     */
    @GetMapping("/publiques")
    public List<Partie> getPublicRoom() {
        return lobbyService.allPubliques();
    }
}
