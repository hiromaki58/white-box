package com.spring.web_game.model;

import java.util.HashSet;
import java.util.Set;

public class GameModel extends BaseModel{
    private String gameTitle;
    private String gameCode;
    private boolean isHightestScoreBest;
    private Set<ScoreModel> scoreList = new HashSet<>();

    GameModel(Long id){
        super();
    }

    public String getGameTitle() {
        return gameTitle;
    }
    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }
    public String getGameCode() {
        return gameCode;
    }
    public void setGameCode(String gameCode) {
        this.gameCode = gameCode;
    }
    public boolean isHightestScoreBest() {
        return isHightestScoreBest;
    }
    public void setHightestScoreBest(boolean isHightestScoreBest) {
        this.isHightestScoreBest = isHightestScoreBest;
    }
    public Set<ScoreModel> getScoreList() {
        return scoreList;
    }
    public void setScoreList(Set<ScoreModel> scoreList) {
        this.scoreList = scoreList;
    }
}
