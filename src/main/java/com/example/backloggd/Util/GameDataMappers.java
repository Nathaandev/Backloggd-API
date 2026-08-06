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
import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import com.example.backloggd.Exceptions.RawgApiException;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Models.ReviewModel;
import com.example.backloggd.Repository.ReviewRepository;
import com.example.backloggd.Services.GameService;
import com.example.backloggd.Services.RawgApiService;
import org.springframework.stereotype.Component;
import org.jsoup.*;

@Component
public class GameDataMappers {
    private final RawgApiService rawgApiService;
    private final ReviewRepository reviewRepository;

    public GameDataMappers(RawgApiService rawgApiService, ReviewRepository reviewRepository) {
        this.rawgApiService = rawgApiService;
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

    public static void ConsolidateGameData(GamesModel gameFound, RawgGameDTO gameWithFullDetails, List<DevelopersDTO> developers, List<GenreDTO> genres, List<PlatformsWrapperDTO> platforms, List<PublishersDTO> publishers) {
        gameFound.setDevelopers(GameDataMappers.DevelopersToString(developers));
        gameFound.setGenres(GameDataMappers.GenresToString(genres));
        gameFound.setPlatforms(GameDataMappers.PlatformsToString(platforms));
        gameFound.setPublishers(GameDataMappers.PublishersToString(publishers));
        String rawDescription = gameWithFullDetails.gameDescription();
        gameFound.setGameDescription(GameDataMappers.cleanHtmlDescription(rawDescription));
    }

    public List<GameSummaryDTO> ConvertRawgResponseToGamesModel(RawgResponseDTO rawgResponse) {
        if (rawgResponse == null || rawgResponse.results() == null || rawgResponse.results().isEmpty()) {
            return List.of();
        }
        return rawgResponse.results().stream()
                .filter(game -> game != null && game.rawgId() != null && game.gameName() != null && !game.gameName().isBlank())
                .map(game -> {

                    RawgGameDTO gameWithFullDetails = rawgApiService.GetGameDetailsWithID(game.rawgId());
                    if (gameWithFullDetails == null) {
                        throw new RawgApiException("IGDB API did not return details for game id " + game.rawgId() + ".");
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
                    GameSummaryDTO gameFound = new GameSummaryDTO(game.rawgId(), game.gameName(), game.releaseDate(), game.metacritic(), genre, platforms,
                            GameDataMappers.cleanHtmlDescription(rawDescription), GameDataMappers.DevelopersToString(gameWithFullDetails.developers()),
                            GameDataMappers.PublishersToString(gameWithFullDetails.publishers()), tags, rating, coverUrl);
                    return gameFound;
                }).collect(Collectors.toList());
    }
}
