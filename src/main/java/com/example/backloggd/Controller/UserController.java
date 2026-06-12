package com.example.backloggd.Controller;

import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    public UserController( UserService userService) {
        this.userService = userService;
    }
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserModel> signup(@RequestBody UserRegistrationDTO userRegistrationDTO){
        return userService.signUp(userRegistrationDTO);
    }
    @PostMapping("/wishlist/{gameName}")
    public ResponseEntity<String> addToWishlist(@PathVariable String gameName, Authentication authentication){
       return userService.addGameToWishlist(gameName, authentication);
    }
    @GetMapping("/userwishlist")
    public ResponseEntity<List<String>> getUserWishlist(Authentication authentication){
        return userService.getUserWishlist(authentication);
    }
    @DeleteMapping("/removefromwishlist/{gameName}")
    public ResponseEntity<String> removeGameFromWishlist(@PathVariable String gameName, Authentication authentication){
        return userService.removeGameFromWishlist(gameName, authentication);
    }
}
