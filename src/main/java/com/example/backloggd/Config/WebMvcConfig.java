package com.example.backloggd.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @RestController
    static class FaviconController {
        @GetMapping("/favicon.ico")
        void returnNoContent() {
            // Returns 204 No Content - prevents error logs
        }
    }
}
