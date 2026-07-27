package com.example.backloggd.DTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record GameReviewDTO(
        @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be at least 0.")
        @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be at most 5.")
        float rating,

        @NotBlank(message = "Review is required.")
        String review,

        @PositiveOrZero(message = "Game time must be zero or greater.")
        int gameTime
) {}
