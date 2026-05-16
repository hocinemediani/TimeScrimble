package com.narbaniki.timescrimble;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    
    private final HashMap<String, ArrayList<DrawMessage>> currentDrawings = new HashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> timersActifs = new ConcurrentHashMap<>();
    private final Map<String, Integer> tempsRestant = new ConcurrentHashMap<>();

    public void saveLine(DrawMessage message, String codePartie) {
        ArrayList<DrawMessage> drawing = (currentDrawings.get(codePartie) == null) ? new ArrayList<>() : currentDrawings.get(codePartie);
        drawing.add(message);
        currentDrawings.put(codePartie, drawing);
    }

    public String getRandomWord() {
        try (Scanner scanner = new Scanner(new File(getClass().getResource("randomWords.txt").getFile()))) {
            int randomNumber = new Random().nextInt(100);
            for (int i = 0; i <= randomNumber; i++) {
                scanner.nextLine();
            }
            return scanner.nextLine();
        } catch (FileNotFoundException e) {
            return "rien";
        }
    }

    public void stopTimer(String codePartie) {
        ScheduledFuture<?> timer = timersActifs.remove(codePartie);
        if (timer != null) {
            timer.cancel(false);
        }
        tempsRestant.remove(codePartie);
    }

    public void initTimer(String codePartie) {
        stopTimer(codePartie);
        tempsRestant.put(codePartie, 60);
        ScheduledFuture<?> timerTask = scheduler.scheduleAtFixedRate(() -> {
            int temps = tempsRestant.get(codePartie) - 1;
            tempsRestant.put(codePartie, temps);
            if (temps <= 0) {
                stopTimer(codePartie);
                Partie partie = partieRepository.findByCode(codePartie);
                if (partie != null) {
                    gererFinManche(codePartie, partie);
                }
            } else {
                Map<String, Object> timeMessage = new HashMap<>();
                timeMessage.put("type", "TIMER");
                timeMessage.put("contenu", temps);
                messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/status", (Object) timeMessage);
            }
        }, 1, 1, TimeUnit.SECONDS);
        timersActifs.put(codePartie, timerTask);
    }

    public void finirPartie(String codePartie, Partie partie) {
        stopTimer(codePartie);
        for (Joueur joueur : partie.getJoueurs()) {
            Map<String, String> secret = new HashMap<>();
            secret.put("score", String.valueOf(joueur.getScoreSession()));
            messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/secret/" + joueur.getPseudo(), secret);
        }
        Map<String, Object> status = new HashMap<>();
        status.put("type", "FIN_PARTIE");
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/status", (Object) status);
    }

    public void lancerManche(String codePartie) {
        initTimer(codePartie);
        Partie partie = partieRepository.findByCode(codePartie);
        if (partie.getManchesJouees() > partie.getJoueurs().size() * 3 - 1) {
            finirPartie(codePartie, partie);
        }
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

    public void gererFinManche(String codePartie, Partie partie) {
        stopTimer(codePartie);
        for (Joueur joueurFinal : partie.getJoueurs()) {
            if (joueurFinal.isEstDessinateur()) {
                joueurFinal.ajouterPoints(600 * partie.getOntDevine() / (partie.getJoueurs().size() - 1));
            } else if (joueurFinal.isADevine()) {
                joueurFinal.ajouterPoints(500 - (joueurFinal.getRangDevinage() * 350) / partie.getJoueurs().size());
            }
            joueurRepository.save(joueurFinal);
        }
        Map<String, Object> status = new HashMap<>();
        status.put("type", "FIN_MANCHE");
        status.put("contenu", partie.getMotADeviner());
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/status", (Object) status);
        partieRepository.save(partie);
        lancerManche(codePartie);
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
        String pseudoInitial = message.getPseudo();
        if (message.getContenu().equalsIgnoreCase(motSecret) && !joueur.isADevine()) {
            joueur.marquerCommeDevine();
            joueur.setRangDevinage(partie.getOntDevine());
            partie.incOntDevine();
            joueurRepository.save(joueur);
            partieRepository.save(partie);
            ChatMessage msgSucces = new ChatMessage(
                "", message.getPseudo() + " (" + joueur.getScoreSession() + " pts)" + " a trouvé le mot !", "SUCCES"
            );
            if (partie.checkFinManche()) {
                gererFinManche(codePartie, partie);
            }
            messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/chat", msgSucces);
        } else {
            message.setPseudo(message.getPseudo() + " (" + joueur.getScoreSession() + "pts)");
            messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/chat", message);
        }
        if ("JOIN".equals(message.getType())) {
            if (currentDrawings.get(codePartie) != null) {
                messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/requestDrawing/" + pseudoInitial, currentDrawings.get(codePartie));
            }
            dispatchMessage(codePartie, "OBTENIR_DESSIN");
        }
    }
}