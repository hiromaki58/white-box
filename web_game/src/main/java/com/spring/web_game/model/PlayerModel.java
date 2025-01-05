package com.spring.web_game.model;

import java.sql.Blob;
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
public class PlayerModel extends BaseModel{
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String familyName;

    @Column(nullable = false, unique = true)
    private String emailAddr;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    private Blob profileImg;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScoreModel> scoreList = new HashSet<>();

    public PlayerModel(Long id, String firstName, String familyName, String emailAddr, String password, Blob profileImg, Set<ScoreModel> scoreList){
        super();
        this.firstName = firstName;
        this.familyName = familyName;
        this.emailAddr = emailAddr;
        this.password = password;
        this.profileImg = profileImg;
        this.scoreList = scoreList;
    }
}
