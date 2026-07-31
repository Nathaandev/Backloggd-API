package com.example.backloggd.DTO;

public record ReviewSummaryDTO(
        String gameName,
        float rating,
        String review,
        int gameTime
) {}
