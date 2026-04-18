package com.narbaniki.timescrimble;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/")
public class LobbyController {

    @Autowired
    private UtilisateurRepository userRepository;
    @Autowired
    private LobbyService lobbyService;
    
    @PostMapping("/create")
    public ResponseEntity<String> createRoom(HttpServletRequest request, HttpServletResponse response,
                            @RequestParam(value = "nom", required = true) String nom,
                            @RequestParam(value = "nbMax", required = true) Integer nb,
                            @RequestParam(value = "isPrivate", required = true) Boolean priv,
                            HttpSession session) {
        try {
            Optional<Utilisateur> userOpt = userRepository.findByApiToken((String) session.getAttribute("apiToken"));
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("");
            }
            Utilisateur user = userOpt.get();
            String code = lobbyService.create(nom, nb, priv, user);
            return ResponseEntity.ok(code);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/join")
    public ResponseEntity<String> joinRoom(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam String code, HttpSession session) throws IOException {
        try {
        Utilisateur user = userRepository.findByApiToken((String) session.getAttribute("apiToken")).get();
        lobbyService.join(code, user);
        return ResponseEntity.ok(code);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }  
    }

    @GetMapping("/publiques")
    public List<Partie> getPublicRoom() {
        return lobbyService.allPrivate();
    }
}