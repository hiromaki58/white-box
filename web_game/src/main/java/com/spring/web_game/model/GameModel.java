package com.spring.web_game.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
public class GameModel extends BaseModel{
    @Column(nullable = false, unique = true)
    private String gameTitle;

    @Column(nullable = true)
    private String gameCode;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isHightestScoreBest;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
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
