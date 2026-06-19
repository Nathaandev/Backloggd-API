package com.example.backloggd.DTO;

public record ReviewResponseDTO(String gameName, String userName,  String review, float rating, int gameTime) {
}
