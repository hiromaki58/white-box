package com.spring.web_game.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ScoreModel extends BaseModel{
    @ManyToOne
    @JoinColumn(name = "gameModel_id", nullable = false)
    private GameModel game;

    @ManyToOne
    @JoinColumn(name = "playermodel_id", nullable = false)
    private PlayerModel player;

    @Column(nullable = false)
    private int gameScore;

    public GameModel getGame() {
        return game;
    }
    public void setGame(GameModel game) {
        this.game = game;
    }
    public PlayerModel getPlayer() {
        return player;
    }
    public void setPlayer(PlayerModel player) {
        this.player = player;
    }
    public int getGameScore() {
        return gameScore;
    }
    public void setGameScore(int gameScore) {
        this.gameScore = gameScore;
    }
}
