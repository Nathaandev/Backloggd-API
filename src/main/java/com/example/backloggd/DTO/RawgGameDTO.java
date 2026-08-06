package com.example.backloggd.DTO;

import java.util.List;

import com.example.backloggd.DTO.ObjectsDTO.DevelopersDTO;
import com.example.backloggd.DTO.ObjectsDTO.GenreDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsWrapperDTO;
import com.example.backloggd.DTO.ObjectsDTO.PublishersDTO;
import com.example.backloggd.DTO.ObjectsDTO.TagsDTO;
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

        List<PlatformsWrapperDTO> platforms,

        List<TagsDTO> tags,

        String coverUrl

         ){
    public RawgGameDTO(Integer rawgId, String gameName, String gameDescription, String releaseDate, List<PublishersDTO> publishers, Integer metacritic, List<DevelopersDTO> developers, List<GenreDTO> genres, List<PlatformsWrapperDTO> platforms, List<TagsDTO> tags) {
        this(rawgId, gameName, gameDescription, releaseDate, publishers, metacritic, developers, genres, platforms, tags, null);
    }
}
