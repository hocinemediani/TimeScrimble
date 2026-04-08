package com.narbaniki.timescrimble;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    
    @GetMapping("/")
    public void home(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("login.html").forward(request, response);
    }

    @GetMapping("/lobby")
    public void lobby(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("lobby.html").forward(request, response);
    }

    @PostMapping("/register")
    public void doRegister(HttpServletRequest request, HttpServletResponse response, @RequestBody String payload, HttpSession session) throws ServletException, IOException {
        String[] payloadArgs = payload.split("&");
        try {
            if (payloadArgs.length == 3) {
                String username = payloadArgs[0].split("=")[1];
                String password = payloadArgs[1].split("=")[1];
                Utilisateur newutilisateur = authService.register(username,password);

                // On garde l'utilisateur en mémoire (Session) pour qu'il reste connecté
                session.setAttribute("utilisateur", newutilisateur);
                request.getRequestDispatcher("lobby.html").forward(request, response);
            } else {
                request.getRequestDispatcher("login.html").forward(request, response);
                throw new IllegalArgumentException("Format invalide : le payload doit contenir exactement 3 arguments.");
            }
        } catch (IllegalArgumentException e) {

        }

    }

    @PostMapping("/login")
    public void doLogin(HttpServletRequest request, HttpServletResponse response,
                        @RequestParam(value = "username", required = true) String username,
                        @RequestParam(value = "password", required = false) String password,
                        @RequestParam(value = "action", required = true) String action,
                        HttpSession session) throws ServletException, IOException {
        try {
            switch(action.toLowerCase()) {
                case "sign-in":
                    Utilisateur utilisateur = authService.connect(username, password);
                    
                    // Sauvegarde dans la session
                    session.setAttribute("utilisateur", utilisateur);
                    response.sendRedirect("lobby.html");
                    break;
                case "guest":
                    response.sendRedirect("login.html");
                    throw new IllegalArgumentException("Format invalide : le payload doit contenir exactement 3 arguments.");
                default:
                    throw new IllegalArgumentException("Cas de base");
                    
            }
        } catch (IllegalArgumentException e) {

        }
    }

}
