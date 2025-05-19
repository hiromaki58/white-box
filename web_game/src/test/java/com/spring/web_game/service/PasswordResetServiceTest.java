package com.spring.web_game.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.spring.web_game.model.PasswordResetTokenModel;
import com.spring.web_game.model.PlayerModel;
import com.spring.web_game.repository.PasswordResetTokenRepository;
import com.spring.web_game.repository.PlayerRepository;

public class PasswordResetServiceTest {
    @Mock
    EmailService emailService;

    @Mock
    PlayerRepository playerRepository;

    @Mock
    PasswordResetTokenRepository passwordResetTokenRepository;

    @InjectMocks
    PasswordResetService passwordResetService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Condition
     *  E-mail is in the DB.
     *  The created token is saved in the DB.
     *
     * Expected result
     *  Call passwordResetService.sendPasswordResetEmail() and send E-mail
     */
    // @Test
    // public void testSendPasswordResetEmailSuccess() {
    //     String emailAddr = "test@example.com";
    //     String passwordResetUrl = "http://localhost:8080/reset-password?token=";

    //     when(playerRepository.existsByEmailAddr(emailAddr)).thenReturn(true);
    //     // To avoid "Cannot invoke "com.spring.web_game.repository.PasswordResetTokenRepository.save(Object)" because "this.passwordResetTokenRepository" is null"
    //     when(passwordResetTokenRepository.save(any())).thenReturn(null);

    //     passwordResetService.sendPasswordResetEmail(emailAddr);

    //     verify(playerRepository, times(1)).existsByEmailAddr(emailAddr);
    //     verify(passwordResetTokenRepository, times(1)).save(any());
    //     verify(emailService, times(1)).sendEmail(eq(emailAddr), anyString(), contains(passwordResetUrl));
    // }

    /**
     * Condition
     *  Valid token in the DB
     *  The token is not expired.
     *  The player is registered.
     *
     * Expected result
     *  Call playerRepository.save() and update the player password
     *  Call passwordResetTokenRepository.deleteByEmailAddr() and delete the used token
     */
    // @Test
    // public void testResetPasswordSuccess() {
    //     String token = "testToken";
    //     String emailAddr = "testEmailAddr";
    //     String newPassword = "newTestPassword";

    //     PasswordResetTokenModel testPasswordResetToken = new PasswordResetTokenModel();
    //     testPasswordResetToken.setToken(token);
    //     testPasswordResetToken.setEmailAddr(emailAddr);
    //     testPasswordResetToken.setExpirationTime(LocalDateTime.now().plusMinutes(10));

    //     PlayerModel testPlayer = new PlayerModel();
    //     testPlayer.setEmailAddr(emailAddr);
    //     testPlayer.setPassword("oldPassword");

    //     when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(testPasswordResetToken));
    //     when(playerRepository.findByEmailAddr(emailAddr)).thenReturn(Optional.of(testPlayer));

    //     boolean result = passwordResetService.resetPassword(token, newPassword);

    //     assertTrue(result);
    //     assertEquals(newPassword, testPlayer.getPassword());

    //     verify(passwordResetTokenRepository, times(1)).findByToken(token);
    //     verify(playerRepository, times(1)).findByEmailAddr(emailAddr);
    //     verify(playerRepository, times(1)).save(testPlayer);
    //     verify(passwordResetTokenRepository, times(1)).deleteByEmailAddr(emailAddr);
    // }
}
