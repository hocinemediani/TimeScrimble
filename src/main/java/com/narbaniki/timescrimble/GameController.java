package com.narbaniki.timescrimble;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

/** La classe GameController permet de gérer l'accès aux salles de jeu.<br>
 * Elle intercepte les requêtes de connexion à une partie et s'assure que :<br>
 * - L'utilisateur est bien authentifié (possède un token API dans sa session),<br>
 * - La partie demandée existe réellement en mémoire.<br>
 * En fonction de ces vérifications, l'utilisateur est redirigé vers le jeu,<br>
 * le lobby ou la page de connexion.
 */
@Controller
public class GameController {

    /** Le service gérant le lobby et les données des parties. */
    private final LobbyService lobbyService;


    /** Créé une instance de GameController.
     * @param lobbyService Le service de gestion du lobby et des parties
     */
    public GameController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }


    /** Permet à un utilisateur de rejoindre une salle de jeu spécifique.<br>
     * La méthode vérifie l'état de la session de l'utilisateur ainsi que<br>
     * l'existence de la partie avant d'autoriser l'accès ou de le rediriger.
     * @param code Le code unique de la partie à rejoindre
     * @param session La session HTTP actuelle de l'utilisateur
     * @return La directive de redirection Spring correspondante (login, lobby ou play.html)
     */
    @GetMapping("/room")
    public String joinRoom(@RequestParam String code, HttpSession session) {
        if (session.getAttribute("apiToken") == null) {
            return "redirect:/login";
        }
        if (lobbyService.getPartie(code) == null) {
            return "redirect:/lobby"; 
        }
        return "forward:/play.html"; 
    }
}
