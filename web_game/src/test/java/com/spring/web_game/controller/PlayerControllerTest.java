package com.spring.web_game.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.spring.web_game.model.PlayerModel;
import com.spring.web_game.repository.PlayerRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class PlayerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PlayerRepository playerRepository;

    @BeforeEach
    public void setUp() {
        playerRepository.deleteAll(); // clear the existing data

        PlayerModel player = new PlayerModel();
        player.setFirstName("John");
        player.setFamilyName("Doe");
        player.setEmailAddr("test@example.com");
        player.setPassword("password123");
        playerRepository.save(player);
    }

    @Test
    public void testLoginSuccess() throws Exception {
        mockMvc.perform(post("/api/player/login")
                .with(csrf())
                .content("{\"emailAddr\": \"test@example.com\", \"password\": \"password123\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loginTry").value(true))
                .andExpect(jsonPath("$.msg").value("Succeed to login"));
    }

    @Test
    public void testLoginFailure() throws Exception {
        mockMvc.perform(post("/api/player/login")
                .content("{\"emailAddr\": \"nonexistent@example.com\", \"password\": \"wrongpassword\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loginTry").value(false))
                .andExpect(jsonPath("$.msg").value("Fail to login"));
    }
}
