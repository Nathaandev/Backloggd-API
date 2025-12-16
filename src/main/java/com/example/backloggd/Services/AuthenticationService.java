package com.example.backloggd.Services;

import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    public String authenticateUser(){
        return "token";
    }
}
