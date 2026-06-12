package com.example.backloggd.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class ReviewModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float rating;

    private String review;

    private int gameTime;

    @ManyToOne
    private UserModel userModel;

    @ManyToOne
    private GamesModel game;

    public ReviewModel( float rating, String review, int gameTime, UserModel userModel, GamesModel game) {
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
}
