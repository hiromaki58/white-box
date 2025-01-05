package com.spring.web_game.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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

    public ScoreModel(GameModel game, PlayerModel player, int gameScore){
        this.game = game;
        this.player = player;
        this.gameScore = gameScore;
    }
}
