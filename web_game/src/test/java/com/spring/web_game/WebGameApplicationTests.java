package com.spring.web_game;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
@ActiveProfiles("test")
class WebGameApplicationTests {
	@MockBean
    private JavaMailSender javaMailSender;

	@Test
	void contextLoads() {
	}
}
