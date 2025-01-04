package com.spring.web_game.model;

public class ScoreModel extends BaseModel{
    private GameModel game;
    private PlayerModel player;
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
