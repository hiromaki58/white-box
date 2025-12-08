package com.spring.web_game.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring.web_game.model.PasswordResetTokenModel;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenModel, Long>{
    Optional<PasswordResetTokenModel> findByToken(String token);
    void deleteByEmailAddr(String emailAddr);
}
