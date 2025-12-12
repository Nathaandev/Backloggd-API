package com.example.backloggd.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record GameSummaryDTO(
        Integer rawgId,

        String gameName,

        String releaseDate,

        Integer metacritic,

        String genres,

        String platforms,

        @JsonIgnore
        String gameDescription,

        @JsonIgnore
        String developers,

        @JsonIgnore
        String publishers
) {
}
