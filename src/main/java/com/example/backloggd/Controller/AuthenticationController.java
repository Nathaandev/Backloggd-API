package com.example.backloggd.Controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {
    private final AuthenticationController authenticationController;

    public AuthenticationController(AuthenticationController authenticationController) {
        this.authenticationController = authenticationController;
    }

    @PostMapping("authenticate")
    public String authenticateUser(){
        return authenticationController.authenticateUser();
    }
}
