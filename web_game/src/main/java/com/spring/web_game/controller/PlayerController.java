package com.spring.web_game.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.web_game.service.PlayerService;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> credentials) {
        String emailAddr = credentials.get("emailAddr");
        String password = credentials.get("password");

        boolean isAuthenticated = playerService.isAuthenticated(emailAddr, password);
        Map<String, Object> res = new HashMap<>();

        if(isAuthenticated){
            res.put("loginTry", true);
            res.put("msg", "Succeed to login");
            return ResponseEntity.ok(res);
        }
        else{
            res.put("loginTry", false);
            res.put("msg", "Fail to login");
            return ResponseEntity.status(401).body(res);
        }
    }
}
