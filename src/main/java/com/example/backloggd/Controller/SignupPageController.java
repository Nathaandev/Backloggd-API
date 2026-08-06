package com.example.backloggd.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SignupPageController {

    @GetMapping("/signup")
    public String signupPage() {
        return "forward:/signup.html";
    }
}
