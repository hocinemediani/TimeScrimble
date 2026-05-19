package com.narbaniki.timescrimble;
 
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
 
/** La classe FinController gère l'accès à la page de fin de partie.
 * Le classement final est transmis directement dans le message WebSocket FIN_PARTIE
 * (stocké côté client en sessionStorage).
 */
@Controller
public class FinController {
 
    /** Sert la page de fin de partie.
     * Redirige vers la page de connexion si le joueur n'est pas authentifié.
     * @param session La session HTTP de l'utilisateur
     * @return
     */
    @GetMapping("/fin")
    public String finPage(HttpSession session) {
        if (session.getAttribute("apiToken") == null) {
            return "redirect:/login";
        }
        return "forward:/fin.html";
    }
    
}
