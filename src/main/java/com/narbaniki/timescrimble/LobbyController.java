package com.narbaniki.timescrimble;
import java.io.IOException;
import java.util.ArrayList;
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
public class LobbyController {

    @Autowired
    private LobbyService lobbyService;
    
        @PostMapping("/create")
        public void createRoom(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(value = "nom", required = true) String nom,
                              @RequestParam(value = "nbMax", required = true) Integer nb,
                              @RequestParam(value = "isPrivate", required = true) Boolean priv,
                              HttpSession session) throws ServletException, IOException {
            try {
                Utilisateur user = (Utilisateur) session.getAttribute("user");
                lobbyService.create(nom, nb, priv, user);
                response.sendRedirect("lobby.html");
            } catch (IllegalArgumentException e) {
                response.sendRedirect("lobby.html");
        }
        }
    
        @PostMapping("/join")
        public void joinRoom(HttpServletRequest request, HttpServletResponse response,
                              @RequestParam(value = "code", required = true) String code,
                              HttpSession session) {
            Utilisateur user = (Utilisateur) session.getAttribute("user");
            lobbyService.join(code, user);
        }

    @GetMapping("/publiques")
    public ArrayList<Partie> getPublicRoom() {
        return lobbyService.AllPrivate();
    }
}