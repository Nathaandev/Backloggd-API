package com.example.backloggd.Util;

import java.util.List;
import java.util.stream.Collectors;

import com.example.backloggd.DTO.ObjectsDTO.DevelopersDTO;
import com.example.backloggd.DTO.ObjectsDTO.GenreDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsWrapperDTO;
import com.example.backloggd.DTO.ObjectsDTO.PublishersDTO;
import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Services.RawgApiService;
import org.jsoup.Jsoup;
import org.springframework.beans.BeanUtils;

public class GameDataMappers {

    public static String GenresToString(List<GenreDTO> genres){
        return genres.stream()
                     .map(GenreDTO::name)
                     .collect(Collectors.joining(", "));
    }
    public static String PlatformsToString(List<PlatformsWrapperDTO> platforms){
        return platforms.stream().map(PlatformsWrapperDTO::platforms)
                                          .map(PlatformsDTO::name).collect(Collectors.joining(", "));
    }
    public static String PublishersToString(List<PublishersDTO> publishers){
        return publishers.stream().map(PublishersDTO::name).collect(Collectors.joining(", "));
    }
    public static String DevelopersToString(List<DevelopersDTO> developers){
        return developers.stream().map(DevelopersDTO::name).collect(Collectors.joining(", "));

    }
    public static String cleanHtmlDescription (String rawHtmlDescription){
        if (rawHtmlDescription == null || rawHtmlDescription.isEmpty()) {
            return "";
        }
        return Jsoup.parse(rawHtmlDescription).text();
    }
    public static void ConsolidateGameData(RawgResponseDTO rawgResponse, GamesModel gameFound, RawgApiService rawgApiService){
        var bestMatch = rawgResponse.results().get(0);
        List<GenreDTO> genres = bestMatch.genres();
        List<PlatformsWrapperDTO> platforms = bestMatch.platforms();
        BeanUtils.copyProperties(bestMatch, gameFound);
        RawgGameDTO gameWithFullDetails = rawgApiService.GetGameDetailsWithID(gameFound.getRawgId());
        List<DevelopersDTO> developers = gameWithFullDetails.developers();
        List<PublishersDTO> publishers = gameWithFullDetails.publishers();
        gameFound.setDevelopers(GameDataMappers.DevelopersToString(developers));
        gameFound.setGenres(GameDataMappers.GenresToString(genres));
        gameFound.setPlatforms(GameDataMappers.PlatformsToString(platforms));
        gameFound.setPublishers(GameDataMappers.PublishersToString(publishers));
        String rawDescription = gameWithFullDetails.gameDescription();
        gameFound.setGameDescription(GameDataMappers.cleanHtmlDescription(rawDescription));
    }

}
