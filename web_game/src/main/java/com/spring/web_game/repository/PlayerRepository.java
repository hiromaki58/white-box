package com.spring.web_game.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring.web_game.model.PlayerModel;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerModel, Long>{
    Optional<PlayerModel> findByEmailAddr(String emailAddr);
}
