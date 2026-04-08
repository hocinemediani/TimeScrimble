package com.narbaniki.timescrimble;

import java.io.IOException;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/")
public class Facade {

    @Autowired
    private AuthService authService;
    
    @GetMapping("/")
    public void home(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("login.html").forward(request, response);
    }

    @PostMapping("/register")
    public void doRegister(@RequestBody String payload, HttpSession session) {
        String[] payloadArgs = payload.split("&");
        try {
            if (payloadArgs.length == 3) {
                String username = payloadArgs[0].split("=")[1];
                String password = payloadArgs[1].split("=")[1];
                Utilisateur newutilisateur = authService.register(username,password);

                // On garde l'utilisateur en mémoire (Session) pour qu'il reste connecté
                session.setAttribute("utilisateur", newutilisateur);
            } else {
                throw new IllegalArgumentException("Format invalide : le payload doit contenir exactement 3 arguments.");
            }
        } catch (IllegalArgumentException e) {

        }

    }

    @PostMapping("/login")
    public void doLogin(@RequestBody String payload, HttpSession session) {
                String[] payloadArgs = payload.split("&");
        try {
            if (payloadArgs.length == 3) {
                String username = payloadArgs[0].split("=")[1];
                String password = payloadArgs[1].split("=")[1];

                Utilisateur utilisateur = authService.connect(username, password);
                
                // Sauvegarde dans la session
                session.setAttribute("utilisateur", utilisateur);

            } else {
                throw new IllegalArgumentException("Format invalide : le payload doit contenir exactement 3 arguments.");
            }
        } catch (IllegalArgumentException e) {

        }
    }

}
