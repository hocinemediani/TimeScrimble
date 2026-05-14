package com.narbaniki.timescrimble;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

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

    public String getRandomWord() {
        try (Scanner scanner = new Scanner(new File(getClass().getResource("randomWords.txt").getFile()))) {
            int randomNumber = new Random().nextInt(30);
            for (int i = 0; i < randomNumber; i++) {
                scanner.nextLine();
            }
            return scanner.nextLine();
        } catch (FileNotFoundException e) {
            return "rien";
        }
    }

    public void lancerManche(String codePartie) {
        Partie partie = partieRepository.findByCode(codePartie);
        partie.preparerNouvelleManche();
        currentDrawings.remove(codePartie);
        String mot = getRandomWord();
        System.out.println(mot);
        partie.setMotADeviner(mot);
        partieRepository.save(partie);
        dispatchMessage(codePartie, "DEBUT_MANCHE");
    }

    public void dispatchMessage(String codePartie, String messageType) {
        Partie partie = partieRepository.findByCode(codePartie);
        Joueur dessinateur = partie.getDessinateurActuel();
        Map<String, Object> status = new HashMap<>();
        status.put("type", messageType);
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
        if ("JOIN".equals(message.getType()) || "LEAVE".equals(message.getType())) {
            message.setContenu(String.valueOf(partie.getJoueurs().size()));
        }
        String motSecret = partie.getMotADeviner();
        Optional<Joueur> joueurOpt = joueurRepository.findByPseudo(message.getPseudo());
        if (joueurOpt.isEmpty()) {
             return;   
        }
        Joueur joueur = joueurOpt.get();
        if (message.getContenu().equalsIgnoreCase(motSecret) && !joueur.isADevine()) {
            joueur.marquerCommeDevine();
            joueurRepository.save(joueur);
            partieRepository.save(partie);
            ChatMessage msgSucces = new ChatMessage(
                "", message.getPseudo() + " a trouvé le mot !", "SUCCES"
            );
            if (partie.checkFinManche()) {
                Map<String, Object> status = new HashMap<>();
                status.put("type", "FIN_MANCHE");
                messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/status", (Object) status);
                lancerManche(codePartie);
            }
            messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/chat", msgSucces);
        } else {
            messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/chat", message);
        }
        if ("JOIN".equals(message.getType())) {
            if (currentDrawings.get(codePartie) != null) {
                messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/requestDrawing/" + message.getPseudo(), currentDrawings.get(codePartie));
            }
            dispatchMessage(codePartie, "OBTENIR_DESSIN");
        }
    }
}