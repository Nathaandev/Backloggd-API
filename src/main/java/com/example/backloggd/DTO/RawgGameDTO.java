package com.example.backloggd.DTO;

import java.util.List;

import com.example.backloggd.DTO.ObjectsDTO.DevelopersDTO;
import com.example.backloggd.DTO.ObjectsDTO.GenreDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsWrapperDTO;
import com.example.backloggd.DTO.ObjectsDTO.PublishersDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RawgGameDTO(
        @JsonProperty("id")
         Integer rawgId,

        @JsonProperty("name")
         String gameName,

        @JsonProperty("description")
         String gameDescription,

        @JsonProperty("released")
         String releaseDate,

        List<PublishersDTO> publishers,

        Integer metacritic,

        List<DevelopersDTO> developers,

        List<GenreDTO> genres,

        List<PlatformsWrapperDTO> platforms
         ){}



