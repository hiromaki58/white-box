package com.spring.web_game.model;

import java.sql.Blob;

import jakarta.persistence.Entity;

@Entity
public class PlayerModel extends BaseModel{
    private String firstName;
    private String familyName;
    private String emailAddr;
    private String password;
    private Blob profileImg;

    public PlayerModel(){
        super();
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
}
