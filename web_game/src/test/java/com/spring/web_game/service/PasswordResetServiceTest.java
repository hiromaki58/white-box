package com.spring.web_game.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @Test
    public void testSendPasswordResetEmail() {
        String emailAddr = "test@example.com";
        String passwordResetUrl = "http://localhost:8080/reset-password?token=";

        //
        when(playerRepository.existsByEmailAddr(emailAddr)).thenReturn(true);
        // To avoid "Cannot invoke "com.spring.web_game.repository.PasswordResetTokenRepository.save(Object)" because "this.passwordResetTokenRepository" is null"
        when(passwordResetTokenRepository.save(any())).thenReturn(null);
        passwordResetService.sendPasswordResetEmail(emailAddr);

        verify(playerRepository, times(1)).existsByEmailAddr(emailAddr);
        verify(passwordResetTokenRepository, times(1)).save(any());
        verify(emailService, times(1)).sendEmail(eq(emailAddr), anyString(), contains(passwordResetUrl));
    }
}
