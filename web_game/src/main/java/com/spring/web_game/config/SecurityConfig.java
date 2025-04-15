package com.spring.web_game.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // CSRFが必要な場合は適宜調整
            .authorizeHttpRequests(requests -> requests
            .requestMatchers("/api/**").permitAll()
            .anyRequest().authenticated())
            .formLogin(login -> login.disable());  // APIのみならこれも無効化OK

        return http.build();
    }
}
