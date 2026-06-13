package com.example.backloggd.DTO;

public record ReviewResponseDTO(String userName, String gameName, String review, float rating, int gameTime) {
}
