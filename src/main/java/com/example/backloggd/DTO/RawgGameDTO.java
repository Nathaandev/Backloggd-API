package com.example.backloggd.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RawgGameDTO(
         @JsonProperty("id")
         Integer rawgId,

         @JsonProperty("name")
         String gameName,

         @JsonProperty("description")
         String gameDescription,

         @JsonProperty("released")
         String releaseDate
         ){}



