package com.spring.web_game.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class PasswordResetTokenModel extends BaseModel{
    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String emailAddr;

    @Column(nullable = false)
    private LocalDateTime expirationTime;

    public PasswordResetTokenModel(){
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}
