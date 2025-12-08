package com.spring.web_game.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.web_game.model.ScoreModel;

public interface ScoreRepository extends JpaRepository<ScoreModel, Long>{
}
