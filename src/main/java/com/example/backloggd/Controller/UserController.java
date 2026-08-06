package com.example.backloggd.Controller;

import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.DTO.UserProfileDTO;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {

    private final UserService userService;

    public UserController( UserService userService) {
        this.userService = userService;
    }

    @ResponseBody
    @PostMapping("/signup")
    public ResponseEntity<UserModel> signup(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO){
        return userService.signUp(userRegistrationDTO);
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "forward:/signup.html";
    }

    @ResponseBody
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(Authentication authentication){
        return userService.getProfile(authentication);
    }
    @ResponseBody
    @PostMapping("/wishlist/{gameName}")
    public ResponseEntity<String> addToWishlist(@Valid @PathVariable String gameName, Authentication authentication){
       return userService.addGameToWishlist(gameName, authentication);
    }
    @ResponseBody
    @GetMapping("/userwishlist")
    public ResponseEntity<List<String>> getUserWishlist(Authentication authentication){
        return userService.getUserWishlist(authentication);
    }
    @ResponseBody
    @DeleteMapping("/removefromwishlist/{gameName}")
    public ResponseEntity<String> removeGameFromWishlist(@Valid @PathVariable String gameName, Authentication authentication){
        return userService.removeGameFromWishlist(gameName, authentication);
    }
}
