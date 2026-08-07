package com.example.backloggd.Services;

import com.example.backloggd.DTO.GameResponseDTO;
import com.example.backloggd.DTO.GameSummaryDTO;
import com.example.backloggd.DTO.ObjectsDTO.DevelopersDTO;
import com.example.backloggd.DTO.ObjectsDTO.GenreDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsWrapperDTO;
import com.example.backloggd.DTO.ObjectsDTO.PublishersDTO;
import com.example.backloggd.DTO.IgdbGameDTO;
import com.example.backloggd.DTO.IgdbResponseDTO;
import com.example.backloggd.Exceptions.IgdbApiException;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final IgdbApiService igdbApiService;
    private final ReviewRepository reviewRepository;

    private final GameDataMappers mapper;

    public GameService(IgdbApiService igdbApiService, ReviewRepository reviewRepository, GameDataMappers mapper) {
        this.igdbApiService = igdbApiService;
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
            IgdbResponseDTO igdbResponse = igdbApiService.getGames(gameName);
            if (igdbResponse == null || igdbResponse.results() == null || igdbResponse.results().isEmpty()) {
                logger.warn("{} was not found in IGDB API.", gameName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game was not found.");
            }
            var bestMatch = igdbResponse.results().get(0);
            if (bestMatch.igdbId() == null) {
                throw new IgdbApiException("IGDB API returned a game without an identifier.");
            }
            List<GenreDTO> genres = safeList(bestMatch.genres());
            List<PlatformsWrapperDTO> platforms = safeList(bestMatch.platforms());
            com.example.backloggd.Models.GamesModel gameFound = new com.example.backloggd.Models.GamesModel();
            BeanUtils.copyProperties(bestMatch, gameFound);
            IgdbGameDTO gameWithFullDetails = igdbApiService.GetGameDetailsWithID(gameFound.getIgdbId());
            if (gameWithFullDetails == null) {
                throw new IgdbApiException("IGDB API did not return full details for game id " + gameFound.getIgdbId() + ".");
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
        IgdbResponseDTO igdbResponse = igdbApiService.getGamesByGenre(genres, pageable);
        if (igdbResponse == null || igdbResponse.results() == null || igdbResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(igdbResponse);
        for (GameSummaryDTO games : gamesFound) {
            checkIfGameIsInDatabase(games.gameName());
        }

        return new PageImpl<>(
                gamesFound,
                pageable,
                igdbResponse.count() == null ? 0 : igdbResponse.count()
        );
    }

    public Page<GameSummaryDTO> searchGameByDeveloper(String developer, Pageable pageable) {
        validateGameName(developer);
        IgdbResponseDTO igdbResponse = igdbApiService.getGamesByDeveloper(developer, pageable);
        if (igdbResponse == null || igdbResponse.results() == null || igdbResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(igdbResponse);

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
                igdbResponse.count() == null ? 0 : igdbResponse.count()
        );
    }

    public Page<GameSummaryDTO> searchGamesByPublishers(String publisher, Pageable pageable) {
        validateGameName(publisher);
        IgdbResponseDTO igdbResponse = igdbApiService.getGamesByPublishers(publisher, pageable);
        if (igdbResponse == null || igdbResponse.results() == null || igdbResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(igdbResponse);

        for (GameSummaryDTO games : gamesFound) {
            checkIfGameIsInDatabase(games.gameName());
        }

        return new PageImpl<>(
                gamesFound,
                pageable,
                igdbResponse.count() == null ? 0 : igdbResponse.count()
        );


    }

    public Page<GameSummaryDTO> searchGamesByMetacritic(String ordering, Pageable pageable) {
        validateGameName(ordering);
        Page<GameSummaryDTO> cachedPage = loadCachedMetacriticGames(ordering, pageable);
        if (cachedPage.hasContent()) {
            return cachedPage;
        }

        IgdbResponseDTO igdbResponse = igdbApiService.getGamesByMetacritic(ordering, pageable);
        if (igdbResponse == null || igdbResponse.results() == null || igdbResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(igdbResponse);
        gamesFound.removeIf(game -> game.metacritic() == null);
        for (GameSummaryDTO games : gamesFound) {
            checkIfGameIsInDatabase(games.gameName());
        }
        return new PageImpl<>(
                gamesFound,
                pageable,
                igdbResponse.count() == null ? 0 : igdbResponse.count()
        );
    }

    public Page<GameSummaryDTO> getPopularGames(String ordering, Pageable pageable) {
        validateGameName(ordering);
        Page<GameSummaryDTO> cachedPage = loadCachedPopularGames(ordering, pageable);
        if (cachedPage.hasContent()) {
            return cachedPage;
        }

        IgdbResponseDTO igdbResponse = igdbApiService.getPopularGames(ordering, pageable);
        if (igdbResponse == null || igdbResponse.results() == null || igdbResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(igdbResponse);
        for (GameSummaryDTO games : gamesFound) {
            checkIfGameIsInDatabase(games.gameName());
        }
        return new PageImpl<>(
                gamesFound,
                pageable,
                igdbResponse.count() == null ? 0 : igdbResponse.count()
        );
    }

    public Page<GameSummaryDTO> searchGamesByTags(String tags, Pageable pageable) {
        validateGameName(tags);
        IgdbResponseDTO igdbResponse = igdbApiService.getGamesByTags(tags, pageable);
        if (igdbResponse == null || igdbResponse.results() == null || igdbResponse.results().isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(igdbResponse);
        for (GameSummaryDTO games : gamesFound) {
            checkIfGameIsInDatabase(games.gameName());
        }
        return new PageImpl<>(
                gamesFound,
                pageable,
                igdbResponse.count() == null ? 0 : igdbResponse.count()
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

    private Page<GameSummaryDTO> loadCachedMetacriticGames(String ordering, Pageable pageable) {
        Sort sort = Sort.by("metacritic");
        if (ordering != null && ordering.trim().toLowerCase().startsWith("-")) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return gameRepository.findByMetacriticIsNotNull(sortedPageable).map(this::toSummaryDto);
    }

    private Page<GameSummaryDTO> loadCachedPopularGames(String ordering, Pageable pageable) {
        Sort sort = Sort.by("hypes");
        if (ordering != null && ordering.trim().toLowerCase().startsWith("-")) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        return gameRepository.findByHypesIsNotNull(sortedPageable).map(this::toSummaryDto);
    }

    private GameSummaryDTO toSummaryDto(GamesModel game) {
        GamesModel resolvedGame = ensureGameDetails(game);
        return new GameSummaryDTO(
                resolvedGame.getIgdbId(),
                resolvedGame.getGameName(),
                resolvedGame.getReleaseDate(),
                resolvedGame.getMetacritic(),
                resolvedGame.getHypes(),
                resolvedGame.getGenres(),
                resolvedGame.getPlatforms(),
                resolvedGame.getGameDescription(),
                resolvedGame.getDevelopers(),
                resolvedGame.getPublishers(),
                "",
                0.0,
                resolvedGame.getCoverUrl()
        );
    }

    private GamesModel ensureGameDetails(GamesModel game) {
        if (game.getCoverUrl() != null && !game.getCoverUrl().isBlank()) {
            return game;
        }
        IgdbResponseDTO igdbResponse = igdbApiService.getGames(game.getGameName());
        if (igdbResponse == null || igdbResponse.results() == null || igdbResponse.results().isEmpty()) {
            return game;
        }

        IgdbGameDTO bestMatch = igdbResponse.results().get(0);
        if (bestMatch == null) {
            return game;
        }

        if (bestMatch.coverUrl() == null || bestMatch.coverUrl().isBlank()) {
            return game;
        }

        BeanUtils.copyProperties(bestMatch, game, "gameDescription", "tags", "rating");
        gameRepository.save(game);
        return game;
    }
}