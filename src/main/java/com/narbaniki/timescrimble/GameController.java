package com.narbaniki.timescrimble;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GameController {

    private final LobbyService lobbyService;

    public GameController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @GetMapping("/room/code/{code}")
    public String joinRoom(@PathVariable String code) {
        Partie partie = lobbyService.getPartie(code);
        if (partie == null) {
            return "redirect:/lobby.html"; 
        }
        return "forward:/play.html"; 
    }
}