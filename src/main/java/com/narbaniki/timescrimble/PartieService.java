package com.narbaniki.timescrimble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PartieService {

    @Autowired
    private PartieRepository partieRepository;

    @Autowired
    private JoueurRepository joueurRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    private HashMap<String, ArrayList<DrawMessage>> currentDrawings = new HashMap<>();

    public void saveLine(DrawMessage message, String codePartie) {
        ArrayList<DrawMessage> drawing = (currentDrawings.get(codePartie) == null) ? new ArrayList<>() : currentDrawings.get(codePartie);
        drawing.add(message);
        currentDrawings.put(codePartie, drawing);
    }

    public void lancerManche(String codePartie) {
        Partie partie = partieRepository.findByCode(codePartie);
        partie.preparerNouvelleManche();
        String mot = "Ilian"; /* Il faut rendre aléatoire le choix du mot. */
        currentDrawings.remove(codePartie);
        partie.setMotADeviner(mot);
        partieRepository.save(partie);
        dispatchMessage(codePartie);
    }

    public void dispatchMessage(String codePartie) {
        Partie partie = partieRepository.findByCode(codePartie);
        Joueur dessinateur = partie.getDessinateurActuel();
        Map<String, Object> status = new HashMap<>();
        status.put("type", "DEBUT_MANCHE");
        status.put("dessinateur", dessinateur.getPseudo());
        status.put("tailleMot", partie.getMotADeviner().length());
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/status", (Object) status);
        Map<String, String> secret = new HashMap<>();
        secret.put("mot", partie.getMotADeviner());
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/secret/" + dessinateur.getPseudo(), secret);
    }

    public void traiterPropositionChat(String codePartie, ChatMessage message) {
        Partie partie = partieRepository.findByCode(codePartie);
        if (partie == null) {
            return;
        }
        if ("JOIN".equals(message.getType())) {
            if (currentDrawings.get(codePartie) != null) {
                messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/requestDrawing/" + message.getPseudo(), currentDrawings.get(codePartie));
            }
            dispatchMessage(codePartie);
        }
        if ("JOIN".equals(message.getType()) || "LEAVE".equals(message.getType())) {
            message.setContenu(String.valueOf(partie.getJoueurs().size()));
        }
        String motSecret = partie.getMotADeviner();
        if (message.getContenu().equalsIgnoreCase(motSecret)) {
            ChatMessage msgSucces = new ChatMessage(
                "", message.getPseudo() + " a trouvé le mot !", "SUCCES"
            );
            messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/chat", msgSucces);
            
            /* Faut plus que mettre à jour le Joueur (aDevine = true) et attribuer les points avec une fonction
            a déterminer. */
            
        } else {
            messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/chat", message);
        }
    }
}