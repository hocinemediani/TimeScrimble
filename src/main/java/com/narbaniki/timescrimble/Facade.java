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

@RestController
@RequestMapping("/")
public class Facade {

    @Autowired
    private AuthService authService;
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @GetMapping("/")
    public void home(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("login.html").forward(request, response);
    }

    @GetMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        home(request, response);
    }

    @GetMapping("/lobby")
    public void lobby(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        if (session.getAttribute("username") != null || session.getAttribute("guestName") != null) {
            response.sendRedirect("lobby.html");
        } else {
            response.sendRedirect("login");
        }
    }

    @PostMapping("/disconnect")
    public void disconnect(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        session.invalidate();
    }

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
                    session.setAttribute("user", utilisateur);
                    session.setAttribute("username", utilisateur.getPseudo());
                    session.setAttribute("apiToken", utilisateur.getApiToken());
                    response.sendRedirect("lobby");
                }
                case "sign-up" -> {
                    Utilisateur utilisateur = authService.register(username, password);
                    session.setAttribute("user", utilisateur);
                    session.setAttribute("username", utilisateur.getPseudo());
                    session.setAttribute("apiToken", utilisateur.getApiToken());
                    response.sendRedirect("lobby");
                }
                case "guest" -> {
                    authService.connectGuest(username);
                    session.setAttribute("guestName", username);
                    response.sendRedirect("lobby");
                }
                default -> response.sendRedirect("login");
            }
        } catch (IllegalArgumentException e) {
            response.sendRedirect("login.html");
        }
    }

}
