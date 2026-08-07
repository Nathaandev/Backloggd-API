package com.example.backloggd.Util;

import java.util.List;
import java.util.stream.Collectors;
import com.example.backloggd.DTO.GameSummaryDTO;
import com.example.backloggd.DTO.ObjectsDTO.DevelopersDTO;
import com.example.backloggd.DTO.ObjectsDTO.GenreDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsWrapperDTO;
import com.example.backloggd.DTO.ObjectsDTO.PublishersDTO;
import com.example.backloggd.DTO.ObjectsDTO.TagsDTO;
import com.example.backloggd.DTO.IgdbGameDTO;
import com.example.backloggd.DTO.IgdbResponseDTO;
import com.example.backloggd.Exceptions.IgdbApiException;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Models.ReviewModel;
import com.example.backloggd.Repository.ReviewRepository;
import com.example.backloggd.Services.GameService;
import com.example.backloggd.Services.IgdbApiService;
import org.springframework.stereotype.Component;
import org.jsoup.*;

@Component
public class GameDataMappers {
    private final IgdbApiService igdbApiService;
    private final ReviewRepository reviewRepository;

    public GameDataMappers(IgdbApiService igdbApiService, ReviewRepository reviewRepository) {
        this.igdbApiService = igdbApiService;
        this.reviewRepository = reviewRepository;
    }

    public static String GenresToString(List<GenreDTO> genres) {
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        return genres.stream()
                .map(GenreDTO::name)
                .collect(Collectors.joining(", "));
    }

    public static String PlatformsToString(List<PlatformsWrapperDTO> platforms) {
        if (platforms == null || platforms.isEmpty()) {
            return "";
        }
        return platforms.stream().map(PlatformsWrapperDTO::platforms)
                .map(PlatformsDTO::name).collect(Collectors.joining(", "));
    }

    public static String PublishersToString(List<PublishersDTO> publishers) {
        if (publishers == null || publishers.isEmpty()) {
            return "";
        }
        return publishers.stream().map(PublishersDTO::name).collect(Collectors.joining(", "));
    }

    public static String DevelopersToString(List<DevelopersDTO> developers) {
        if (developers == null || developers.isEmpty()) {
            return "";
        }
        return developers.stream().map(DevelopersDTO::name).collect(Collectors.joining(", "));

    }

    public static String TagsToString(List<TagsDTO> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return tags.stream().map(TagsDTO::name).collect(Collectors.joining(", "));
    }

    public static String cleanHtmlDescription(String rawHtmlDescription) {
        if (rawHtmlDescription == null || rawHtmlDescription.isEmpty()) {
            return "";
        }
        return Jsoup.parse(rawHtmlDescription).text();
    }

    public static void ConsolidateGameData(GamesModel gameFound, IgdbGameDTO gameWithFullDetails, List<DevelopersDTO> developers, List<GenreDTO> genres, List<PlatformsWrapperDTO> platforms, List<PublishersDTO> publishers) {
        gameFound.setDevelopers(GameDataMappers.DevelopersToString(developers));
        gameFound.setGenres(GameDataMappers.GenresToString(genres));
        gameFound.setPlatforms(GameDataMappers.PlatformsToString(platforms));
        gameFound.setPublishers(GameDataMappers.PublishersToString(publishers));
        gameFound.setCoverUrl(gameWithFullDetails.coverUrl());
        String rawDescription = gameWithFullDetails.gameDescription();
        gameFound.setGameDescription(GameDataMappers.cleanHtmlDescription(rawDescription));
    }

    public List<GameSummaryDTO> ConvertRawgResponseToGamesModel(IgdbResponseDTO rawgResponse) {
        if (rawgResponse == null || rawgResponse.results() == null || rawgResponse.results().isEmpty()) {
            return List.of();
        }
        return rawgResponse.results().stream()
                .filter(game -> game != null && game.igdbId() != null && game.gameName() != null && !game.gameName().isBlank())
                .map(game -> {

                    IgdbGameDTO gameWithFullDetails = igdbApiService.GetGameDetailsWithID(game.igdbId());
                    if (gameWithFullDetails == null) {
                        throw new IgdbApiException("IGDB API did not return details for game id " + game.igdbId() + ".");
                    }
                    String rawDescription = gameWithFullDetails.gameDescription();
                    String genre = GameDataMappers.GenresToString(game.genres());
                    String platforms = GameDataMappers.PlatformsToString(game.platforms());
                    String tags = GameDataMappers.TagsToString(game.tags());
                    List<ReviewModel> reviews = reviewRepository.findByGameGameName(game.gameName());
                    if (reviews == null) {
                        reviews = List.of();
                    }
                    double rating = reviews.stream()
                            .mapToDouble(ReviewModel::getRating)
                            .average()
                            .orElse(0.0);
                    String coverUrl = gameWithFullDetails.coverUrl();
                    if (coverUrl == null || coverUrl.isBlank()) {
                        coverUrl = game.coverUrl();
                    }
                    GameSummaryDTO gameFound = new GameSummaryDTO(game.igdbId(), game.gameName(), game.releaseDate(), game.metacritic(), game.igdbRating(), genre, platforms,
                            GameDataMappers.cleanHtmlDescription(rawDescription), GameDataMappers.DevelopersToString(gameWithFullDetails.developers()),
                            GameDataMappers.PublishersToString(gameWithFullDetails.publishers()), tags, rating, coverUrl);
                    return gameFound;
                }).collect(Collectors.toList());
    }
}
