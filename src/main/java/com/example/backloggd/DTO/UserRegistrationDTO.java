package com.example.backloggd.DTO;

import jakarta.validation.constraints.NotBlank;

public record UserRegistrationDTO(
        @NotBlank(message = "Username is required.")
        String userName,

        @NotBlank(message = "Password is required.")
        String password
) {
}
