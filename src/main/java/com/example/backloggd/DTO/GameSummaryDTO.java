package com.example.backloggd.DTO;

public record GameSummaryDTO(
        Integer rawgId,

        String gameName,

        String releaseDate,

        Integer metacritic,

        String genres,

        String platforms
) {
}
