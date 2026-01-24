package com.example.backloggd.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

//Used if i'm making a a requisition that receive many games
public record GameSummaryDTO(
        Integer rawgId,

        String gameName,

        String releaseDate,

        Integer metacritic,

        String genres,

        String platforms,

        @JsonIgnore
        String gameDescription,
        
        String developers,

        @JsonIgnore
        String publishers
) {
}
