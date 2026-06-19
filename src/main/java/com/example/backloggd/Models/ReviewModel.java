package com.example.backloggd.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.Authentication;

import java.util.Optional;

@Entity
@Table(name = "reviews")
public class ReviewModel {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gameName;

    private String userName;

    private float rating;

    private String review;

    private int gameTime;

    @JsonIgnore
    @ManyToOne
    private UserModel userModel;

    @JsonIgnore
    @ManyToOne
    private GamesModel game;

    public ReviewModel(Long id, String gameName, String userName, float rating, String review, int gameTime, UserModel userModel, GamesModel game) {
        this.id = id;
        this.gameName = gameName;
        this.userName = userName;
        this.rating = rating;
        this.review = review;
        this.gameTime = gameTime;
        this.userModel = userModel;
        this.game = game;
    }

    public ReviewModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public int getGameTime() {
        return gameTime;
    }

    public void setGameTime(int gameTime) {
        this.gameTime = gameTime;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    public GamesModel getGame() {
        return game;
    }

    public void setGame(GamesModel game) {
        this.game = game;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
