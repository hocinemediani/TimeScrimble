package com.narbaniki.timescrimble;

import java.io.IOException;
import java.util.Optional;

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
    
    @GetMapping("/")
    public void home(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("login.html").forward(request, response);
    }

    @PostMapping("/login")
    public void doLogin(@RequestBody String payload) {
        String[] payloadArgs = payload.split("&");
        String username = payloadArgs[0].split("=")[1];
        String action;
        Optional<String> password = Optional.empty();
        if (payloadArgs.length == 3) {
            action = payloadArgs[2].split("=")[1].toLowerCase();
            password = payloadArgs[1].split("=")[1].describeConstable();
        } else {
            action = "guest";
        }
        String passwordString = password.isPresent() ? password.get() : "no password.";
        System.out.println("The user : " + username + " tried to " + action + " with password " + passwordString);
    }

}
