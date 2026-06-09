package com.example.backloggd.Models;

import com.example.backloggd.DTO.UserRegistrationDTO;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "users")
public class UserModel {
    
    @ManyToMany
    private List<GamesModel> wishlist;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String userName;

    private String password;

    private String userEmail;

    public UserModel(UserRegistrationDTO userRegistrationDTO) {
    }

    public UserModel(UserModel byUserName) {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<GamesModel> getWishlist() {
        return wishlist;
    }

    public void setWishlist(List<GamesModel> wishlist) {
        this.wishlist = wishlist;
    }

    public UserModel(String userName, String password, String userEmail) {
        this.userName = userName;
        this.password = password;
        this.userEmail = userEmail;
    }

    public UserModel() {
    }


}
