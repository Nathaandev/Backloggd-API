package com.example.backloggd.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

//Used if i am making a requisition that receive many games
public record GameSummaryDTO(
        Integer igdbId,

        String gameName,

        String releaseDate,

        Integer metacritic,

        Integer hypes,

        String genres,

        String platforms,

        @JsonIgnore
        String gameDescription,

        String developers,

        String publishers,

        String tags,

        double rating,

        String coverUrl
) {
    public GameSummaryDTO(Integer igdbId, String gameName, String releaseDate, Integer metacritic, String genres, String platforms, String gameDescription, String developers, String publishers, String tags, double rating) {
        this(igdbId, gameName, releaseDate, metacritic, null, genres, platforms, gameDescription, developers, publishers, tags, rating, null);
    }
}
