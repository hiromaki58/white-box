package com.spring.web_game.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class ScoreModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private GameModel game;
    private PlayerModel player;
    private int gameScore;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScoreModel other = (ScoreModel) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
