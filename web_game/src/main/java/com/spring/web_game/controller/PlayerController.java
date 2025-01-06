package com.spring.web_game.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.web_game.service.PlayerService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("api/player")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService){
        this.playerService = playerService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String emailAddr, String password) {
        boolean isAuthenticated = playerService.isAuthenticated(emailAddr, password);

        if(isAuthenticated){
            return ResponseEntity.ok("Succeed to login");
        }
        else{
            return ResponseEntity.status(401).body("Fail to login");
        }
    }
}
