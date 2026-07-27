package com.example.backloggd.Services;

import com.example.backloggd.DTO.GameResponseDTO;
import com.example.backloggd.DTO.GameSummaryDTO;
import com.example.backloggd.DTO.ObjectsDTO.DevelopersDTO;
import com.example.backloggd.DTO.ObjectsDTO.GenreDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsWrapperDTO;
import com.example.backloggd.DTO.ObjectsDTO.PublishersDTO;
import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import com.example.backloggd.Exceptions.RawgApiException;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Models.ReviewModel;
import com.example.backloggd.Repository.GameRepository;
import com.example.backloggd.Repository.ReviewRepository;
import com.example.backloggd.Util.GameDataMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class GameService {

    Logger logger = LoggerFactory.getLogger(GameService.class);

    @Autowired
    GameRepository gameRepository;
    private final RawgApiService rawgApiService;
    private final ReviewRepository reviewRepository;

    private final GameDataMappers mapper;

    public GameService(RawgApiService rawgApiService, ReviewRepository reviewRepository, GameDataMappers mapper) {
        this.rawgApiService = rawgApiService;
        this.reviewRepository = reviewRepository;
        this.mapper = mapper;
    }

    public double calculateGameRating(String gameName) {
        validateGameName(gameName);
        List<ReviewModel> reviews = Optional.ofNullable(reviewRepository.findByGameGameName(gameName))
                .orElse(List.of());
        double ratings = reviews.stream()
                .mapToDouble(ReviewModel::getRating)
                .average()
                .orElse(0.0);
        return ratings;
    }

    public ResponseEntity<String> checkIfGameIsInDatabase(String gameName) {
        validateGameName(gameName);
        logger.info("Checking if {} is in database...", gameName);
        var gamesModelOptional = gameRepository.findBygameNameIgnoreCase(gameName);
        if (gamesModelOptional.isEmpty()) {
            RawgResponseDTO rawgResponse = rawgApiService.getGames(gameName);
            if (rawgResponse == null || rawgResponse.results() == null || rawgResponse.results().isEmpty()) {
                logger.warn("{} was not found in RAWG API.", gameName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game was not found.");
            }
            var bestMatch = rawgResponse.results().get(0);
            if (bestMatch.rawgId() == null) {
                throw new RawgApiException("RAWG API returned a game without an identifier.");
            }
            List<GenreDTO> genres = safeList(bestMatch.genres());
            List<PlatformsWrapperDTO> platforms = safeList(bestMatch.platforms());
            com.example.backloggd.Models.GamesModel gameFound = new com.example.backloggd.Models.GamesModel();
            BeanUtils.copyProperties(bestMatch, gameFound);
            RawgGameDTO gameWithFullDetails = rawgApiService.GetGameDetailsWithID(gameFound.getRawgId());
            if (gameWithFullDetails == null) {
                throw new RawgApiException("RAWG API did not return full details for game id " + gameFound.getRawgId() + ".");
            }
            List<DevelopersDTO> developers = safeList(gameWithFullDetails.developers());
            List<PublishersDTO> publishers = safeList(gameWithFullDetails.publishers());

            GameDataMappers.ConsolidateGameData(gameFound, gameWithFullDetails, developers, genres, platforms, publishers);
            gameRepository.save(gameFound);
            logger.info("{} was added to database.", gameName);
            return ResponseEntity.ok("Game added to database.");
        } else {
            logger.info("{} was found in database.", gameName);
            return ResponseEntity.ok("Game found in database.");
        }
    }

    public ResponseEntity<GameResponseDTO> searchGame(String gameName) {
        validateGameName(gameName);
        checkIfGameIsInDatabase(gameName);
        var gamesModelOptional = gameRepository.findBygameNameIgnoreCase(gameName);
        var game = gamesModelOptional.orElseThrow(() -> new IllegalArgumentException("Game not found."));
        List<ReviewModel> reviews = Optional.ofNullable(reviewRepository.findByGameGameName(gameName))
                .orElse(List.of());
        double rating = calculateGameRating(gameName);
        GameResponseDTO gameResponseDTO = new GameResponseDTO(game.getGameName(), game.getGameDescription(), game.getReleaseDate(), game.getPublishers(), game.getMetacritic(), game.getDevelopers(), game.getGenres(), game.getPlatforms(), rating, reviews);
        return ResponseEntity.ok(gameResponseDTO);
    }

    public Page<GameSummaryDTO> searchGameByGenre(String genres, Pageable pageable) {
        validateGameName(genres);
        RawgResponseDTO rawgResponse = rawgApiService.getGamesByGenre(genres, pageable);
        if (rawgResponse == null || rawgResponse.results() == null || rawgResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(rawgResponse);
        for (GameSummaryDTO games : gamesFound) {
            checkIfGameIsInDatabase(games.gameName());
        }

        return new PageImpl<>(
                gamesFound,
                pageable,
                rawgResponse.count() == null ? 0 : rawgResponse.count()
        );
    }

    public Page<GameSummaryDTO> searchGameByDeveloper(String developer, Pageable pageable) {
        validateGameName(developer);
        RawgResponseDTO rawgResponse = rawgApiService.getGamesByDeveloper(developer, pageable);
        if (rawgResponse == null || rawgResponse.results() == null || rawgResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(rawgResponse);

        for (GameSummaryDTO gameSummaryDTO : gamesFound) {
            Optional<GamesModel> gameOptional = gameRepository.findBygameNameIgnoreCase(gameSummaryDTO.gameName());
            if (gameOptional.isEmpty()) {
                GamesModel game = new GamesModel();
                BeanUtils.copyProperties(gameSummaryDTO, game);
                gameRepository.save(game);
            }
        }
        return new PageImpl<>(
                gamesFound,
                pageable,
                rawgResponse.count() == null ? 0 : rawgResponse.count()
        );
    }

    public Page<GameSummaryDTO> searchGamesByPublishers(String publisher, Pageable pageable) {
        validateGameName(publisher);
        RawgResponseDTO rawgResponse = rawgApiService.getGamesByPublishers(publisher, pageable);
        if (rawgResponse == null || rawgResponse.results() == null || rawgResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(rawgResponse);

        for (GameSummaryDTO games : gamesFound) {
            checkIfGameIsInDatabase(games.gameName());
        }

        return new PageImpl<>(
                gamesFound,
                pageable,
                rawgResponse.count() == null ? 0 : rawgResponse.count()
        );


    }

    public Page<GameSummaryDTO> searchGamesByMetacritic(String ordering, Pageable pageable) {
        validateGameName(ordering);
        RawgResponseDTO rawgResponse = rawgApiService.getGamesByMetacritic(ordering, pageable);
        if (rawgResponse == null || rawgResponse.results() == null || rawgResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(rawgResponse);
        gamesFound.removeIf(game -> game.metacritic() == null);
        for (GameSummaryDTO games : gamesFound) {
            checkIfGameIsInDatabase(games.gameName());
        }
        return new PageImpl<>(
                gamesFound,
                pageable,
                rawgResponse.count() == null ? 0 : rawgResponse.count()
        );
    }

    public Page<GameSummaryDTO> searchGamesByTags(String tags, Pageable pageable) {
        validateGameName(tags);
        RawgResponseDTO rawgResponse = rawgApiService.getGamesByTags(tags, pageable);
        if (rawgResponse == null || rawgResponse.results() == null || rawgResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(rawgResponse);
        for (GameSummaryDTO games : gamesFound) {
            checkIfGameIsInDatabase(games.gameName());
        }
        return new PageImpl<>(
                gamesFound,
                pageable,
                rawgResponse.count() == null ? 0 : rawgResponse.count()
        );
    }

    private void validateGameName(String gameName) {
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Game name is required.");
        }
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}