package com.example.backloggd.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GameSummaryDTO(
        @JsonProperty("id")
        Integer rawgId,

        @JsonProperty("name")
        String gameName,

        @JsonProperty("released")
        String releaseDate,

        Integer metacritic,

        String genres,

        String platforms
) {
}
