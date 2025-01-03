package com.spring.web_game.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class GameModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String gameTitle;
    private String gameCode;
    private boolean isHightestScoreBest;
    private Set<ScoreModel> scoreList = new HashSet<>();

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
        GameModel other = (GameModel) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
