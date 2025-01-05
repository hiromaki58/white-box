package com.spring.web_game.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.web_game.model.PlayerModel;

public interface PlayerRepository extends JpaRepository<PlayerModel, Long>{
}
