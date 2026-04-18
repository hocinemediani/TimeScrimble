package com.narbaniki.timescrimble;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class GameController {

    private final LobbyService lobbyService;

    public GameController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @GetMapping("/room")
    public String joinRoom(@RequestParam String code, HttpSession session) {
        Partie partie = lobbyService.getPartie(code);
        if (session.getAttribute("apiToken") == null) {
            return "redirect:/login";
        }
        if (partie == null) {
            return "redirect:/lobby"; 
        }
        return "forward:/play.html"; 
    }
}