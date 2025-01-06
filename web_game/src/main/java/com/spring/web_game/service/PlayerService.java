package com.spring.web_game.service;

import org.springframework.stereotype.Service;

import com.spring.web_game.repository.PlayerRepository;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository){
        this.playerRepository = playerRepository;
    }

    public boolean isAuthenticated(String emailAddr, String password){
        return playerRepository.findByEmailAddr(emailAddr)
                .map(playerModel -> playerModel.getPassword().equals(password))
                .orElse(false);
    }
}
