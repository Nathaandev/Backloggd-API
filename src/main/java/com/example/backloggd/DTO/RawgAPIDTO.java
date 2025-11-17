package com.example.backloggd.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RawgAPIDTO(
         @JsonProperty("id")
         Long gameId,

         @JsonProperty("name")
         String gameName,

         @JsonProperty("description")
         String gameDescription ){}

