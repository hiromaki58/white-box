package com.spring.web_game.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.web_game.model.GameModel;

public interface GameRepository extends JpaRepository<GameModel, Long>{
}
