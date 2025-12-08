package com.spring.web_game.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.spring.web_game.model.PlayerModel;
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

    public boolean isRegistrationCompleted(PlayerModel playerModel){
        // Try to avoid same E-mai address registration
        boolean isExistingPlayer = playerRepository.existsByEmailAddr(playerModel.getEmailAddr());
        if(isExistingPlayer){
            // add the function to show the msg "The E-mail address is already used."
            return false;
        }

        try{
            playerRepository.save(playerModel);
            return true;
        }
        catch(DataIntegrityViolationException e){
            // add the function to show the msg "The E-mail address is already used."
            return false;
        }
    }
}
