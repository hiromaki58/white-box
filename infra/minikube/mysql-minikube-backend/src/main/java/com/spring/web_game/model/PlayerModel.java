package com.spring.web_game.model;

import java.sql.Blob;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

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

    public PlayerModel(){
    }

    public PlayerModel(Long id, String firstName, String familyName, String emailAddr, String password, Blob profileImg, Set<ScoreModel> scoreList){
        super();
        this.firstName = firstName;
        this.familyName = familyName;
        this.emailAddr = emailAddr;
        this.password = password;
        this.profileImg = profileImg;
        this.scoreList = scoreList;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Blob getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(Blob profileImg) {
        this.profileImg = profileImg;
    }

    public Set<ScoreModel> getScoreList() {
        return scoreList;
    }

    public void setScoreList(Set<ScoreModel> scoreList) {
        this.scoreList = scoreList;
    }
}
