package com.example.backloggd.Util;

import java.util.List;
import java.util.stream.Collectors;
import com.example.backloggd.DTO.GameSummaryDTO;
import com.example.backloggd.DTO.ObjectsDTO.DevelopersDTO;
import com.example.backloggd.DTO.ObjectsDTO.GenreDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsWrapperDTO;
import com.example.backloggd.DTO.ObjectsDTO.PublishersDTO;
import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Services.RawgApiService;
import org.springframework.stereotype.Component;
import org.jsoup.*;

@Component
public class GameDataMappers {
    private final RawgApiService rawgApiService;
    public GameDataMappers(RawgApiService rawgApiService) {
        this.rawgApiService = rawgApiService;
    }

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

    public static void ConsolidateGameData(GamesModel gameFound, RawgGameDTO gameWithFullDetails, List<DevelopersDTO> developers, List<GenreDTO> genres, List<PlatformsWrapperDTO> platforms, List<PublishersDTO> publishers){
        gameFound.setDevelopers(GameDataMappers.DevelopersToString(developers));
        gameFound.setGenres(GameDataMappers.GenresToString(genres));
        gameFound.setPlatforms(GameDataMappers.PlatformsToString(platforms));
        gameFound.setPublishers(GameDataMappers.PublishersToString(publishers));
        String rawDescription = gameWithFullDetails.gameDescription();
        gameFound.setGameDescription(GameDataMappers.cleanHtmlDescription(rawDescription));
    }
    public List<GameSummaryDTO> ConvertRawgResponseToGamesModel(RawgResponseDTO rawgResponse){
        return rawgResponse.results().stream().map(game -> {

            RawgGameDTO gameWithFullDetails = rawgApiService.GetGameDetailsWithID(game.rawgId());
            String rawDescription = gameWithFullDetails.gameDescription();
            String genre = GameDataMappers.GenresToString(game.genres());
            String platforms = GameDataMappers.PlatformsToString(game.platforms());
            GameSummaryDTO gameFound = new GameSummaryDTO(game.rawgId(), game.gameName(), game.releaseDate(), game.metacritic(), genre, platforms,
                    GameDataMappers.cleanHtmlDescription(rawDescription), GameDataMappers.DevelopersToString(gameWithFullDetails.developers()),
                    GameDataMappers.PublishersToString(gameWithFullDetails.publishers()));
            return gameFound;
        }).collect(Collectors.toList());
    }
}
