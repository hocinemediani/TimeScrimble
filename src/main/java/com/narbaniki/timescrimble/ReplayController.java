package com.narbaniki.timescrimble;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

/** La classe ReplayController expose l'endpoint POST /replay permettant de recréer<br>
 * une nouvelle partie avec les mêmes paramètres (nom, confidentialité, capacité max)<br>
 * que la partie qui vient de se terminer.<br>
 * Les paramètres sont transmis depuis fin.html, où ils ont été stockés en sessionStorage<br>
 * lors de la réception du message WebSocket FIN_PARTIE.
 */
@RestController
public class ReplayController {

    /** Le répertoire de parties. */
    @Autowired
    private PartieRepository partieRepository;


    /** Crée une nouvelle partie avec les mêmes paramètres que la partie terminée
     * et retourne son code d'accès pour que le client puisse s'y rediriger immédiatement.
     * @param nom          Le nom à donner à la nouvelle partie
     * @param estPrivee    Vrai si la nouvelle partie doit être privée
     * @param nbJoueursMax Le nombre maximum de joueurs autorisés
     * @param session      La session HTTP de l'utilisateur
     * @return
     */
    @PostMapping("/replay")
    public ResponseEntity<HashMap<String, String>> replay(
            @RequestParam String nom,
            @RequestParam(defaultValue = "true") boolean estPrivee,
            @RequestParam(defaultValue = "8")    int nbJoueursMax,
            HttpSession session) {
 
        if (session.getAttribute("apiToken") == null) {
            return ResponseEntity.status(401).build();
        }
 
        Partie nouvellePartie = new Partie(nom, estPrivee, nbJoueursMax);
        partieRepository.save(nouvellePartie);
 
        HashMap<String, String> response = new HashMap<>();
        response.put("code", nouvellePartie.getCode());

        return ResponseEntity.ok(response);
    }
}