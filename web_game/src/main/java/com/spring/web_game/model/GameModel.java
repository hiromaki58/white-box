package com.spring.web_game.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
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

    public GameModel(Long id, String gameTitle, String gameCode, boolean isHightestScoreBest, Set<ScoreModel> scoreList){
        super(id);
        this.gameTitle = gameTitle;
        this.gameCode = gameCode;
        this.isHightestScoreBest = isHightestScoreBest;
        this.scoreList = scoreList;
    }
}
