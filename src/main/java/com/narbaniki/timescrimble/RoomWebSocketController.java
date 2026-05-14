package com.narbaniki.timescrimble;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class RoomWebSocketController {

    @Autowired
    private PartieService partieService;

    @Autowired
    private PartieRepository partieRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/room/{code}/draw")
    @Transactional
    public void gererDessin(@DestinationVariable String code, DrawMessage message) {
        Partie partie = partieRepository.findByCode(code);
        if (partie.getDessinateurActuel().getPseudo().equals(message.getPseudo())) {
            partieService.saveLine(message, code);
            messagingTemplate.convertAndSend("/topic/room/" + code + "/draw", message);
        }
    }

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

    @MessageMapping("/room/{code}/chat")
    @Transactional
    public void gererChat(@DestinationVariable String code, ChatMessage message) {
        partieService.traiterPropositionChat(code, message);
    }
}