package com.narbaniki.timescrimble;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/** La classe facade expose quelques services de base à l'utilisateur, accessibles<br>
 * via des endpoints API.<br>
 * Le services proposés sont notamment :<br>
 * - L'accès à la page de connexion sur accès au serveur web,<br>
 * - L'accès au lobby permettant de créer ou rejoindre une partie,<br>
 * - L'accès aux données de l'utilsiteur par le biais de son token API,<br>
 * - L'accès à une méthode de déconnexion de l'utilisateur,<br>
 * - L'accès aux données relatives à une partie via le code de partie.
 */
@RestController
@RequestMapping("/")
public class Facade {

    /** Le service d'authentification. */
    @Autowired
    private AuthService authService;
    /** Le répertoire d'utilisateurs. */
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    /** Le répertoire de parties. */
    @Autowired
    private PartieRepository partieRepository;


    /** Permet de se connecter au serveur web et d'être redirigé suivant<br>
     * l'état de sa connexion.
     * @param request La requête envoyée par l'utilisateur
     * @param response La réponse associée
     * @param session La session (si existante) de l'utilisateur
     * @throws IOException Si le serveur n'arrive pas à rediriger l'utilisateur
     * @throws ServletException Si le serveur n'arrive pas à rediriger l'utilisateur
     */
    @GetMapping("/")
    public void home(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException, ServletException {
        if (session.getAttribute("username") != null || session.getAttribute("guestName") != null) {
            lobby(request, response, session);
        } else {
            login(request, response, session);
        }
    }


    /** Permet de s'authentifier auprès du serveur.<br>
     * L'utilisateur authentifié se voit automatiquement renvoyé vers la page de lobby<br>
     * et peut se connecter à n'importe quelle partie disponible.
     * @param request La requête envoyée par l'utilisateur
     * @param response La réponse associée
     * @param session La session (si existante) de l'utilisateur
     * @throws IOException Si le serveur n'arrive pas à rediriger l'utilisateur
     * @throws ServletException Si le serveur n'arrive pas à rediriger l'utilisateur
     */
    @GetMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException, ServletException {
        if (session.getAttribute("username") != null || session.getAttribute("guestName") != null) {
            lobby(request, response, session);
        } else {
            request.getRequestDispatcher("login.html").forward(request, response);
        }
    }


    /** Permet un accès au lobby des parties à l'utilisateur.<br>
     * Le lobby permet de créer ou de rejoindre une partie.
     * @param request La requête envoyée par l'utilisateur
     * @param response La réponse associée
     * @param session La session (si existante) de l'utilisateur
     * @throws IOException Si le serveur n'arrive pas à rediriger l'utilisateur
     * @throws ServletException Si le serveur n'arrive pas à rediriger l'utilisateur
     */
    @GetMapping("/lobby")
    public void lobby(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException, ServletException {
        if (session.getAttribute("username") != null || session.getAttribute("guestName") != null) {
            request.getRequestDispatcher("lobby.html").forward(request, response);
        } else {
            login(request, response, session);
        }
    }


    /** Permet de se déconnecter en invalidant la session de l'utilisateur.
     * @param request La requête envoyée par l'utilisateur
     * @param response La réponse associée
     * @param session La session (si existante) de l'utilisateur
     */
    @PostMapping("/disconnect")
    public void disconnect(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        session.invalidate();
    }


    /** Permet de récupérer des informations liées à la partie, telles que :<br>
     * - Le code de la partie (déjà connu mais utile),<br>
     * - Le nombre de joueur actuel dans la partie,<br>
     * - Le nom de la partie (à afficher dans le titre de la page).
     * @param code Le code unique associé à la partie
     * @param request La requête envoyée par l'utilisateur
     * @param response La réponse associée
     */
    @GetMapping("/roominfo")
    public Map<String, String> roomInfo(@RequestParam String code, HttpServletRequest request, HttpServletResponse response) {
        Partie partie = partieRepository.findByCode(code);
        if (partie == null) {
            System.out.println("Aucune partie ne possède ce code.");
            return Collections.emptyMap();
        }
        HashMap<String, String> infos = new HashMap<>();
        infos.put("code", code);
        infos.put("playerCount", Integer.toString(partie.getJoueurs().size()));
        infos.put("name", partie.getNom());
        infos.put("host", partie.getHost().getPseudo());
        return infos;
    }


    /** Permet de récupérer des informations liées au joueur, telles que :<br>
     * - Le nom d'utilisateur du joueur,<br>
     * - Le nombre de victoires actuelles du joueur,<br>
     * - Le nombre de défaites du joueur.
     * @param request La requête envoyée par l'utilisateur
     * @param response La réponse associée
     * @param session La session (si existante) de l'utilisateur
     */
    @GetMapping("/userinfo")
    public Map<String, String> userInfo(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String apiToken = (String) session.getAttribute("apiToken");
        Optional<Utilisateur> userOpt = utilisateurRepository.findByApiToken(apiToken);
        if (userOpt.isEmpty()) {
            System.out.println("Aucun utilisateur ne possède ce token.");
            return Collections.emptyMap();
        }
        Utilisateur user = userOpt.get();
        HashMap<String, String> infos = new HashMap<>();
        infos.put("username", user.getPseudo());
        infos.put("wins", Integer.toString(user.getVictoires()));
        infos.put("losses", Integer.toString(user.getTotalParties() - user.getVictoires()));
        return infos;
    }


    /** Permet de s'authentifier auprès du serveur et d'obtenir une session valide.<br>
     * L'utilisateur authentifié se voit automatiquement renvoyé vers la page de lobby<br>
     * et peut se connecter à n'importe quelle partie disponible.<br>
     * Cette fonction traite la requête et connecte/créé le compte de l'utilisateur.
     * @param request La requête envoyée par l'utilisateur
     * @param response La réponse associée
     * @param session La session (si existante) de l'utilisateur
     * @param username Le nom d'utilisateur du joueur
     * @param password Le mot de passe (en clair) de l'utilisateur
     * @param action L'action à effectuer pour l'utilisateur
     * @throws IOException Si le serveur n'arrive pas à rediriger l'utilisateur
     */
    @PostMapping("/login")
    public void doLogin(HttpServletRequest request, HttpServletResponse response,
                        @RequestParam(value = "username", required = true) String username,
                        @RequestParam(value = "password", required = false) String password,
                        @RequestParam(value = "action", required = true) String action,
                        HttpSession session) throws IOException {
        try {
            switch(action.toLowerCase()) {
                case "sign-in" -> {
                    Utilisateur utilisateur = authService.connect(username, password);
                    session.setAttribute("username", utilisateur.getPseudo());
                    session.setAttribute("apiToken", utilisateur.getApiToken());
                    response.sendRedirect("lobby");
                }
                case "sign-up" -> {
                    Utilisateur utilisateur = authService.register(username, password);
                    session.setAttribute("username", utilisateur.getPseudo());
                    session.setAttribute("apiToken", utilisateur.getApiToken());
                    response.sendRedirect("lobby");
                }
                case "guest" -> {
                    Utilisateur utilisateur = authService.connectGuest(username);
                    session.setAttribute("guestName", username);
                    session.setAttribute("apiToken", utilisateur.getApiToken());
                    response.sendRedirect("lobby");
                }
                default -> response.sendRedirect("login");
            }
        } catch (IllegalArgumentException e) {
            response.sendRedirect("login.html");
        }
    }

}
