package com.spring.web_game.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.spring.web_game.model.PasswordResetTokenModel;
import com.spring.web_game.model.PlayerModel;
import com.spring.web_game.repository.PasswordResetTokenRepository;
import com.spring.web_game.repository.PlayerRepository;

@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PlayerRepository playerRepository;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository, PlayerRepository playerRepository){
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.playerRepository = playerRepository;
    }

    /**
     * Add a token to make sure the e-mail reciever is the genuine player to change the password
     * @param emailAddr
     * @return
     */
    public boolean sendPasswordResetEmail(String emailAddr){
        if(!playerRepository.existsByEmailAddr(emailAddr)){
            return false;
        }

        passwordResetTokenRepository.deleteByEmailAddr(emailAddr);

        String token = UUID.randomUUID().toString();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(15);

        PasswordResetTokenModel passwordResetToken = new PasswordResetTokenModel();
        passwordResetToken.setToken(token);
        passwordResetToken.setEmailAddr(emailAddr);
        passwordResetToken.setExpirationTime(expirationTime);
        passwordResetTokenRepository.save(passwordResetToken);

        String passwordResetUrl = "http://localhost:8080/reset-password?token=" + token;

        return true;
    }

    /**
     * The genuine players update the password with checking the token
     * @param token
     * @param newPassword
     * @return
     */
    public boolean resetPassword(String token, String newPassword){
        PasswordResetTokenModel retrivedToken = passwordResetTokenRepository.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if(retrivedToken.getExpirationTime().isBefore(LocalDateTime.now())){
            new IllegalArgumentException("The token is expired.");
        }

        Optional<PlayerModel> retrivedPlayer = playerRepository.findByEmailAddr(retrivedToken.getEmailAddr());
        if(retrivedPlayer.isPresent()){
            PlayerModel updatingPlayer = retrivedPlayer.get();
            updatingPlayer.setPassword(newPassword);
            playerRepository.save(updatingPlayer);

            passwordResetTokenRepository.deleteByEmailAddr(retrivedToken.getEmailAddr());
        }
        else{
            throw new IllegalArgumentException("Player not found");
        }

        return true;
    }
}
