package com.narbaniki.timescrimble;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

/** La classe PartieService contient la logique métier principale du jeu en temps réel.<br>
 * Elle gère l'ensemble du cycle de vie d'une partie et de ses manches, notamment :<br>
 * - Le système de chronomètre (timer) synchronisé entre les joueurs,<br>
 * - L'évaluation des propositions faites dans le chat par rapport au mot secret,<br>
 * - L'attribution des points de victoire au dessinateur et aux devineurs,<br>
 * - L'envoi des différents messages WebSocket (dessin, chat, statuts).
 */
@Service
public class PartieService {

    /** Le répertoire de parties. */
    @Autowired
    private PartieRepository partieRepository;

    /** Le répertoire de joueurs. */
    @Autowired
    private JoueurRepository joueurRepository;

    /** Le répertoire d'utilisateurs. */
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    /** Le service d'envoi de messages. */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /** Dictionnaire stockant temporairement en mémoire les traits dessinés pour chaque partie.<br>
     * Clé : Le code de la partie. Valeur : La liste des messages de dessin. */
    private final HashMap<String, ArrayList<DrawMessage>> currentDrawings = new HashMap<>();

    /** Service d'exécution planifiée gérant les threads des chronomètres. */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    /** Dictionnaire répertoriant les tâches (chronomètres) en cours d'exécution pour chaque partie. */
    private final Map<String, ScheduledFuture<?>> timersActifs = new ConcurrentHashMap<>();

    /** Dictionnaire associant à chaque code de partie le temps restant (en secondes) pour la manche. */
    private final Map<String, Integer> tempsRestant = new ConcurrentHashMap<>();


    /** Sauvegarde un message de dessin (trait) en mémoire pour une partie donnée.<br>
     * Cela permet d'envoyer l'historique complet du dessin aux joueurs rejoignant la partie en cours.
     * @param message Le message contenant les données du segment dessiné
     * @param codePartie Le code unique de la partie concernée
     */
    public void saveLine(DrawMessage message, String codePartie) {
        ArrayList<DrawMessage> drawing = (currentDrawings.get(codePartie) == null) ? new ArrayList<>() : currentDrawings.get(codePartie);
        drawing.add(message);
        currentDrawings.put(codePartie, drawing);
    }


    /** Sélectionne et retourne un mot de manière aléatoire depuis le fichier texte "randomWords.txt".
     * @return Un mot pioché aléatoirement, ou la chaîne "rien" en cas d'erreur de lecture du fichier
     */
    public String getRandomWord() {
        try (Scanner scanner = new Scanner(new File(getClass().getResource("randomWords.txt").getFile()))) {
            int randomNumber = new Random().nextInt(100);
            for (int i = 0; i < randomNumber; i++) {
                scanner.nextLine();
            }
            return scanner.nextLine();
        } catch (FileNotFoundException e) {
            return "rien";
        }
    }


    /** Arrête, annule et supprime le chronomètre actuellement actif pour une partie spécifique.
     * @param codePartie Le code de la partie dont le chronomètre doit être interrompu
     */
    public void stopTimer(String codePartie) {
        ScheduledFuture<?> timer = timersActifs.remove(codePartie);
        if (timer != null) {
            timer.cancel(false);
        }
        tempsRestant.remove(codePartie);
    }


    /** Initialise et lance un nouveau chronomètre de 60 secondes pour une manche.<br>
     * À chaque seconde écoulée, le temps restant est diffusé via WebSocket aux joueurs.<br>
     * Lorsque le temps atteint 0, la méthode déclenche automatiquement la fin de la manche.
     * @param codePartie Le code de la partie concernée par ce chronomètre
     */
    public void initTimer(String codePartie) {
        stopTimer(codePartie);
        tempsRestant.put(codePartie, 60);
        ScheduledFuture<?> timerTask = scheduler.scheduleAtFixedRate(() -> tickTimer(codePartie), 1, 1, TimeUnit.SECONDS);
        timersActifs.put(codePartie, timerTask);
    }


    /** Gère une itération (une seconde) du chronomètre pour une partie.
     * @param codePartie Le code de la partie concernée
     */
    private void tickTimer(String codePartie) {
        int temps = tempsRestant.get(codePartie) - 1;
        tempsRestant.put(codePartie, temps);
        if (temps <= 0) {
            stopTimer(codePartie);
            Partie partie = partieRepository.findByCode(codePartie);
            if (partie != null) {
                gererFinManche(codePartie, partie);
            }
        } else {
            diffuserTempsRestant(codePartie, temps);
        }
    }


    /** Diffuse le temps restant de la manche en cours via WebSocket.
     * @param codePartie Le code de la partie
     * @param temps Le temps restant en secondes
     */
    private void diffuserTempsRestant(String codePartie, int temps) {
        Map<String, Object> timeMessage = new HashMap<>();
        timeMessage.put("type", "TIMER");
        timeMessage.put("contenu", temps);
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/status", (Object) timeMessage);
    }


    /** Gère la procédure de fin d'une partie complète.<br>
     * Cette méthode arrête le chronomètre, met à jour les statistiques persistantes des utilisateurs<br>
     * (nombre de parties jouées, victoires), informe les clients de la fin du jeu via WebSocket,<br>
     * puis supprime définitivement la partie de la base de données.
     * @param codePartie Le code de la partie se terminant
     * @param partie L'entité Partie correspondante
     */
    public void finirPartie(String codePartie, Partie partie) {
        stopTimer(codePartie);
        List<Joueur> joueursCopie = new ArrayList<>(partie.getJoueurs());
        List<String> leaderboard = partie.getLeaderboard();
        for (Joueur joueur : joueursCopie) {
            envoyerScoreFinal(codePartie, joueur);
            mettreAJourStatistiquesUtilisateur(joueur, leaderboard);
            partie.retirerJoueur(joueur);
        }
        partieRepository.save(partie);
        diffuserFinPartie(codePartie);
        partieRepository.delete(partie);
    }


    /** Envoie secrètement le score final d'un joueur à la fin de la partie.
     * @param codePartie Le code de la partie
     * @param joueur Le joueur concerné
     */
    private void envoyerScoreFinal(String codePartie, Joueur joueur) {
        Map<String, String> secret = new HashMap<>();
        secret.put("score", String.valueOf(joueur.getScoreSession()));
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/secret/" + joueur.getPseudo(), secret);
    }


    /** Met à jour les statistiques globales (parties jouées, victoires) d'un utilisateur en BDD.
     * @param joueur Le joueur dont on doit mettre à jour les statistiques
     * @param leaderboard Le classement final de la partie
     */
    private void mettreAJourStatistiquesUtilisateur(Joueur joueur, List<String> leaderboard) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByUsername(joueur.getPseudo());
        if (utilisateurOpt.isPresent()) {
            Utilisateur utilisateur = utilisateurOpt.get();
            utilisateur.incrementerParties();
            if (!leaderboard.isEmpty() && leaderboard.get(0).equals(utilisateur.getPseudo())) {
                utilisateur.incrementerVictoires();
            }
            utilisateurRepository.save(utilisateur);
        }
    }


    /** Notifie tous les clients de la salle que la partie est officiellement terminée.
     * @param codePartie Le code de la partie
     */
    private void diffuserFinPartie(String codePartie) {
        Map<String, Object> status = new HashMap<>();
        status.put("type", "FIN_PARTIE");
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/status", (Object) status);
    }


    /** Prépare les données et démarre une toute nouvelle manche pour la partie.<br>
     * Modifie l'état de la partie, sélectionne un nouveau mot secret à deviner, réinitialise<br>
     * l'historique du dessin en mémoire, et notifie les joueurs du début de manche.
     * @param codePartie Le code de la partie concernée
     */
    public void lancerManche(String codePartie) {
        initTimer(codePartie);
        Partie partie = partieRepository.findByCode(codePartie);
        partie.preparerNouvelleManche();
        currentDrawings.remove(codePartie);
        String mot = getRandomWord();
        partie.setMotADeviner(mot);
        partieRepository.save(partie);
        dispatchMessage(codePartie, "DEBUT_MANCHE");
    }


    /** Diffuse un message de statut général à tous les joueurs d'une salle.<br>
     * Cette méthode s'occupe également d'envoyer secrètement, via un canal dédié, le mot<br>
     * que le dessinateur actuel devra faire deviner.
     * @param codePartie Le code de la partie
     * @param messageType Le type d'événement à diffuser (par exemple : "DEBUT_MANCHE")
     */
    public void dispatchMessage(String codePartie, String messageType) {
        Partie partie = partieRepository.findByCode(codePartie);
        Joueur dessinateur = partie.getDessinateurActuel();
        Map<String, Object> status = new HashMap<>();
        status.put("type", messageType);
        String dessinateurPseudo = (dessinateur == null) ? "" : dessinateur.getPseudo();
        status.put("dessinateur", dessinateurPseudo);
        status.put("tailleMot", partie.getMotADeviner().length());
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/status", (Object) status);
        Map<String, String> secret = new HashMap<>();
        secret.put("mot", partie.getMotADeviner());
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/secret/" + dessinateurPseudo, secret);
    }


    /** Gère le processus de fin d'une manche en cours.<br>
     * Arrête le chronomètre, calcule et attribue les points remportés par le dessinateur et<br>
     * par les joueurs ayant deviné, notifie les clients de la fin de la manche,<br>
     * puis détermine si une nouvelle manche doit être lancée ou si la partie entière est terminée.
     * @param codePartie Le code de la partie
     * @param partie L'entité Partie en cours de mise à jour
     */
    public void gererFinManche(String codePartie, Partie partie) {
        stopTimer(codePartie);
        attribuerPointsManche(partie);
        Map<String, Object> status = new HashMap<>();
        status.put("type", "FIN_MANCHE");
        status.put("contenu", partie.getMotADeviner());
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/status", (Object) status);
        partieRepository.save(partie);
        if (partie.getManchesJouees() > partie.getJoueurs().size() * 3 - 1) {
            finirPartie(codePartie, partie);
        } else {
            lancerManche(codePartie);
        }
    }


    /** Calcule et attribue les points gagnés durant la manche aux différents joueurs.
     * @param partie L'entité Partie concernée
     */
    private void attribuerPointsManche(Partie partie) {
        int nbJoueurs = partie.getJoueurs().size();
        if (nbJoueurs <= 1) return;
        for (Joueur joueurFinal : partie.getJoueurs()) {
            if (joueurFinal.isEstDessinateur()) {
                joueurFinal.ajouterPoints(600 * partie.getOntDevine() / (nbJoueurs - 1));
            } else if (joueurFinal.isADevine()) {
                joueurFinal.ajouterPoints(500 - (joueurFinal.getRangDevinage() * 350) / nbJoueurs);
            }
            joueurRepository.save(joueurFinal);
        }
    }


    /** Intercepte et traite les propositions textuelles envoyées par les joueurs dans le chat.<br>
     * Vérifie si le message envoyé correspond exactement au mot secret de la manche.<br>
     * Si c'est le cas, valide la découverte pour le joueur, lui accorde un statut de réussite,<br>
     * et masque le mot secret dans le chat. S'occupe également d'envoyer l'historique du dessin<br>
     * aux joueurs qui viennent de rejoindre la partie (message de type JOIN).
     * @param codePartie Le code de la partie dans laquelle le message est envoyé
     * @param message L'objet ChatMessage contenant les informations et le contenu du message
     */
    public void traiterPropositionChat(String codePartie, ChatMessage message) {
        Partie partie = partieRepository.findByCode(codePartie);
        if (partie == null) return;
        gererAffichageRejoindreQuitter(partie, message);
        Optional<Joueur> joueurOpt = joueurRepository.findByPseudo(message.getPseudo());
        if (joueurOpt.isEmpty()) return;
        Joueur joueur = joueurOpt.get();
        String pseudoInitial = message.getPseudo();
        boolean motTrouve = verifierProposition(codePartie, partie, joueur, message);
        if (!motTrouve) {
            diffuserMessageChatStandard(codePartie, message, joueur);
        }
        if ("JOIN".equals(message.getType())) {
            synchroniserDessinNouveauJoueur(codePartie, pseudoInitial);
        }
    }


    /** Intercepte les événements de connexion/déconnexion pour masquer le contenu et afficher le nombre de joueurs.
     * @param partie La partie concernée
     * @param message Le message envoyé
     */
    private void gererAffichageRejoindreQuitter(Partie partie, ChatMessage message) {
        if ("JOIN".equals(message.getType()) || "LEAVE".equals(message.getType())) {
            message.setContenu(String.valueOf(partie.getJoueurs().size()));
        }
    }


    /** Vérifie si le joueur a trouvé le mot secret dans son message de chat.<br>
     * Un dessinateur ne peut pas trouver le propre mot qu'il dessine.
     * @param codePartie Le code de la partie
     * @param partie L'entité Partie
     * @param joueur Le joueur ayant envoyé le message
     * @param message Le message contenant la proposition textuelle
     * @return Vrai si le mot a été deviné, faux sinon
     */
    private boolean verifierProposition(String codePartie, Partie partie, Joueur joueur, ChatMessage message) {
        if (message.getContenu().equalsIgnoreCase(partie.getMotADeviner()) 
            && !joueur.isADevine() 
            && !joueur.isEstDessinateur()) {
            
            validerBonneReponse(codePartie, partie, joueur, message);
            return true;
        }
        return false;
    }


    /** Valide la bonne réponse d'un joueur, attribue le rang de devinage et annonce le succès sur le chat.
     * @param codePartie Le code de la partie
     * @param partie L'entité Partie
     * @param joueur Le joueur ayant deviné
     * @param message Le message initial
     */
    private void validerBonneReponse(String codePartie, Partie partie, Joueur joueur, ChatMessage message) {
        joueur.setDevine(true);
        joueur.setRangDevinage(partie.getOntDevine());
        partie.incOntDevine();
        joueurRepository.save(joueur);
        partieRepository.save(partie);
        ChatMessage msgSucces = new ChatMessage(
            "", message.getPseudo() + " (" + joueur.getScoreSession() + " pts) a trouvé le mot !", "SUCCES"
        );
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/chat", msgSucces);
        if (partie.checkFinManche()) {
            gererFinManche(codePartie, partie);
        }
    }


    /** Diffuse un message textuel classique dans le chat en ajoutant le score actuel du joueur.
     * @param codePartie Le code de la partie
     * @param message Le message à envoyer
     * @param joueur Le joueur ayant écrit le message
     */
    private void diffuserMessageChatStandard(String codePartie, ChatMessage message, Joueur joueur) {
        message.setPseudo(message.getPseudo() + " (" + joueur.getScoreSession() + "pts)");
        messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/chat", message);
    }


    /** Envoie tout l'historique de dessin actuel en mémoire au joueur venant de rejoindre la partie.
     * @param codePartie Le code de la partie
     * @param pseudoInitial Le pseudonyme du joueur rejoignant la partie
     */
    private void synchroniserDessinNouveauJoueur(String codePartie, String pseudoInitial) {
        if (currentDrawings.containsKey(codePartie)) {
            messagingTemplate.convertAndSend("/topic/room/" + codePartie + "/requestDrawing/" + pseudoInitial, currentDrawings.get(codePartie));
        }
        dispatchMessage(codePartie, "OBTENIR_DESSIN");
    }
}