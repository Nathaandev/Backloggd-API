package com.example.backloggd.Controller;

import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
