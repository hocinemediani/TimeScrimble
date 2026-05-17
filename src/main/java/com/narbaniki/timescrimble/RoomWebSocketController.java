package com.narbaniki.timescrimble;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

/** La classe RoomWebSocketController est un contrôleur WebSocket gérant les interactions<br>
 * en temps réel au sein d'une salle de jeu.<br>
 * Elle intercepte les messages envoyés par les clients sur des canaux spécifiques et<br>
 * les redirige vers la logique métier appropriée. Les principales actions gérées sont :<br>
 * - La réception et la diffusion des données de dessin,<br>
 * - La requête de lancement de la partie par l'hôte,<br>
 * - La réception et le traitement des propositions faites dans le chat.
 */
@Controller
public class RoomWebSocketController {

    /** Le service contenant la logique métier de la partie. */
    @Autowired
    private PartieService partieService;

    /** Le répertoire permettant l'accès aux données des parties en base de données. */
    @Autowired
    private PartieRepository partieRepository;

    /** Le template utilisé pour router et envoyer les messages WebSocket aux clients. */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    /** Réceptionne les messages de dessin envoyés par un client et les rediffuse à tous<br>
     * les joueurs de la salle abonnés au canal de dessin.<br>
     * Cette méthode vérifie au préalable que le joueur envoyant le message est bien<br>
     * le dessinateur actuel de la manche pour éviter toute triche.
     * @param code Le code unique de la partie concernée
     * @param message L'objet DrawMessage contenant les informations des traits dessinés
     */
    @MessageMapping("/room/{code}/draw")
    @Transactional
    public void gererDessin(@DestinationVariable String code, DrawMessage message) {
        Partie partie = partieRepository.findByCode(code);
        if (partie.getDessinateurActuel().getPseudo().equals(message.getPseudo())) {
            partieService.saveLine(message, code);
            messagingTemplate.convertAndSend("/topic/room/" + code + "/draw", message);
        }
    }


    /** Intercepte la requête de lancement de la partie (généralement initiée par l'hôte).<br>
     * Vérifie que la partie est bien en attente, modifie son statut pour la démarrer,<br>
     * puis fait appel au service pour déclencher la toute première manche.
     * @param code Le code unique de la partie à démarrer
     */
    @MessageMapping("/room/{code}/start")
    @Transactional
    public void gererDemarrage(@DestinationVariable String code) {
        Partie partie = partieRepository.findByCode(code);
        if (partie != null && partie.getStatut() == StatutPartie.ATTENTE) {
            partie.demarrer();
            partieRepository.save(partie);
            partieService.lancerManche(code); 
        }
    }


    /** Réceptionne les messages textuels du chat envoyés par les joueurs de la salle.<br>
     * Délègue immédiatement le traitement de ce message au PartieService afin d'évaluer<br>
     * si la proposition correspond au mot secret ou s'il s'agit d'une simple discussion.
     * @param code Le code unique de la partie concernée
     * @param message L'objet ChatMessage contenant le contenu textuel et le pseudonyme de l'expéditeur
     */
    @MessageMapping("/room/{code}/chat")
    @Transactional
    public void gererChat(@DestinationVariable String code, ChatMessage message) {
        partieService.traiterPropositionChat(code, message);
    }
}
