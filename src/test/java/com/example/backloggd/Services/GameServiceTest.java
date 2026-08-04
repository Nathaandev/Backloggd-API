package com.example.backloggd.Services;

import com.example.backloggd.DTO.GameResponseDTO;
import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import com.example.backloggd.DTO.GameSummaryDTO;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Models.ReviewModel;
import com.example.backloggd.Repository.GameRepository;
import com.example.backloggd.Repository.ReviewRepository;
import com.example.backloggd.Util.GameDataMappers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RawgApiService rawgApiService;

    @Mock
    private GameDataMappers mapper;

    private GameService gameService;

    @BeforeEach
    void setup() {
        gameService = spy(new GameService(rawgApiService, reviewRepository, mapper));
        gameService.gameRepository = gameRepository;
    }

    @Test
    void calculateGameRatingReturnsAverageForExistingReviews() {
        ReviewModel reviewOne = new ReviewModel();
        reviewOne.setRating(4.0f);
        ReviewModel reviewTwo = new ReviewModel();
        reviewTwo.setRating(2.0f);

        when(reviewRepository.findByGameGameName("Hades")).thenReturn(List.of(reviewOne, reviewTwo));

        double rating = gameService.calculateGameRating("Hades");

        assertEquals(3.0d, rating);
    }

    @Test
    void calculateGameRating_returnsZeroWhenNoReviews() {
        when(reviewRepository.findByGameGameName("NoReviews")).thenReturn(List.of());

        double rating = gameService.calculateGameRating("NoReviews");

        assertEquals(0.0d, rating);
    }

    @Test
    void calculateGameRating_throwsWhenGameNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> gameService.calculateGameRating(null));
    }

    @Test
    void calculateGameRating_throwsWhenGameNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> gameService.calculateGameRating("   "));
    }

    @Test
    void searchGameReturnsGameResponseWithReviewsAndRating() {
        GamesModel game = new GamesModel();
        game.setGameName("Hades");
        game.setGameDescription("A dungeon crawler");
        ReviewModel review = new ReviewModel();
        review.setRating(4.5f);
        review.setGameName("Hades");

        doReturn(ResponseEntity.ok("Game found in database.")).when(gameService).checkIfGameIsInDatabase("Hades");
        when(gameRepository.findBygameNameIgnoreCase("Hades")).thenReturn(Optional.of(game));
        when(reviewRepository.findByGameGameName("Hades")).thenReturn(List.of(review));

        ResponseEntity<GameResponseDTO> response = gameService.searchGame("Hades");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Hades", response.getBody().gameName());
        assertEquals(4.5d, response.getBody().rating());
        assertEquals(1, response.getBody().reviews().size());
    }

    @Test
    void checkIfGameIsInDatabaseReturnsNotFoundWhenRawgReturnsNothing() {
        when(gameRepository.findBygameNameIgnoreCase("Unknown")).thenReturn(Optional.empty());
        when(rawgApiService.getGames("Unknown")).thenReturn(new RawgResponseDTO(List.of(), 0));

        ResponseEntity<String> response = gameService.checkIfGameIsInDatabase("Unknown");

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Game was not found.", response.getBody());
    }

    @Test
    void checkIfGameIsInDatabase_returnsFoundWhenAlreadyPersisted() {
        GamesModel game = new GamesModel();
        game.setGameName("Persisted");
        when(gameRepository.findBygameNameIgnoreCase("Persisted")).thenReturn(Optional.of(game));

        var resp = gameService.checkIfGameIsInDatabase("Persisted");

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("Game found in database.", resp.getBody());
        verify(rawgApiService, never()).getGames(anyString());
    }

    @Test
    void checkIfGameIsInDatabase_addsGameWhenRawgReturnsMatch() {
        when(gameRepository.findBygameNameIgnoreCase("NewGame")).thenReturn(Optional.empty());

        RawgGameDTO bestMatch = new RawgGameDTO(123, "NewGame", "desc", "2020-01-01", List.of(), 90, List.of(), List.of(), List.of(), List.of());
        RawgResponseDTO response = new RawgResponseDTO(List.of(bestMatch), 1);
        when(rawgApiService.getGames("NewGame")).thenReturn(response);

        RawgGameDTO fullDetails = new RawgGameDTO(123, "NewGame", "<p>full</p>", "2020-01-01", List.of(), 90, List.of(), List.of(), List.of(), List.of());
        when(rawgApiService.GetGameDetailsWithID(123)).thenReturn(fullDetails);

        var resp = gameService.checkIfGameIsInDatabase("NewGame");

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("Game added to database.", resp.getBody());
        verify(gameRepository).save(any(GamesModel.class));
    }

    @Test
    void checkIfGameIsInDatabase_throwsWhenRawgReturnsMatchWithoutId() {
        when(gameRepository.findBygameNameIgnoreCase("NoId")).thenReturn(Optional.empty());
        RawgGameDTO bestMatch = new RawgGameDTO(null, "NoId", "desc", "2020-01-01", List.of(), 50, List.of(), List.of(), List.of(), List.of());
        RawgResponseDTO response = new RawgResponseDTO(List.of(bestMatch), 1);
        when(rawgApiService.getGames("NoId")).thenReturn(response);

        assertThrows(com.example.backloggd.Exceptions.RawgApiException.class, () -> gameService.checkIfGameIsInDatabase("NoId"));
    }

    @Test
    void checkIfGameIsInDatabase_throwsWhenGetGameDetailsReturnsNull() {
        when(gameRepository.findBygameNameIgnoreCase("MissingDetails")).thenReturn(Optional.empty());
        RawgGameDTO bestMatch = new RawgGameDTO(555, "MissingDetails", "desc", "2020-01-01", List.of(), 50, List.of(), List.of(), List.of(), List.of());
        RawgResponseDTO response = new RawgResponseDTO(List.of(bestMatch), 1);
        when(rawgApiService.getGames("MissingDetails")).thenReturn(response);
        when(rawgApiService.GetGameDetailsWithID(555)).thenReturn(null);

        assertThrows(com.example.backloggd.Exceptions.RawgApiException.class, () -> gameService.checkIfGameIsInDatabase("MissingDetails"));
    }

    @Test
    void searchGame_throwsWhenGameNotFoundAfterLookup() {
        when(gameRepository.findBygameNameIgnoreCase("Ghost")).thenReturn(Optional.empty());
        when(rawgApiService.getGames("Ghost")).thenReturn(new RawgResponseDTO(List.of(), 0));

        assertThrows(IllegalArgumentException.class, () -> gameService.searchGame("Ghost"));
    }

    @Test
    void searchGameByGenre_returnsEmptyPageWhenRawgReturnsNoResults() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(rawgApiService.getGamesByGenre("rpg", pageable)).thenReturn(new RawgResponseDTO(List.of(), 0));

        Page<?> page = gameService.searchGameByGenre("rpg", pageable);

        assertEquals(0, page.getTotalElements());
        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void searchGameByGenre_invokesCheckIfGameIsInDatabaseForEachResult() {
        PageRequest pageable = PageRequest.of(0, 10);
        RawgGameDTO g1 = new RawgGameDTO(1, "G1", "d", "2020-01-01", List.of(), 10, List.of(), List.of(), List.of(), List.of());
        RawgGameDTO g2 = new RawgGameDTO(2, "G2", "d", "2020-01-01", List.of(), 20, List.of(), List.of(), List.of(), List.of());
        RawgResponseDTO response = new RawgResponseDTO(List.of(g1, g2), 2);
        when(rawgApiService.getGamesByGenre("action", pageable)).thenReturn(response);
        when(mapper.ConvertRawgResponseToGamesModel(response)).thenReturn(new java.util.ArrayList<>(List.of(
                new GameSummaryDTO(1, "G1", "2020-01-01", 10, "", "", "desc", "dev", "pub", "", 0.0),
                new GameSummaryDTO(2, "G2", "2020-01-01", 20, "", "", "desc", "dev", "pub", "", 0.0)
        )));

        gameService.searchGameByGenre("action", pageable);

        verify(gameService, times(2)).checkIfGameIsInDatabase(anyString());
    }

    @Test
    void searchGamesByPublishers_callsRawgWithPublisherParameter() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(rawgApiService.getGamesByPublishers("Ubisoft", pageable)).thenReturn(new RawgResponseDTO(List.of(), 0));

        gameService.searchGamesByPublishers("Ubisoft", pageable);

        verify(rawgApiService).getGamesByPublishers("Ubisoft", pageable);
    }

    @Test
    void searchGamesByTags_callsRawgWithTagsParameter() {
        PageRequest pageable = PageRequest.of(0, 5);
        when(rawgApiService.getGamesByTags("multiplayer", pageable)).thenReturn(new RawgResponseDTO(List.of(), 0));

        gameService.searchGamesByTags("multiplayer", pageable);

        verify(rawgApiService).getGamesByTags("multiplayer", pageable);
    }

    @Test
    void searchGamesByMetacritic_returnsEmptyPageWhenRawgReturnsNoResults() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(rawgApiService.getGamesByMetacritic("desc", pageable)).thenReturn(new RawgResponseDTO(List.of(), 0));

        var page = gameService.searchGamesByMetacritic("desc", pageable);

        assertEquals(0, page.getTotalElements());
    }

    @Test
    void searchGamesByMetacritic_filtersNullMetacriticAndChecksDatabase() {
        PageRequest pageable = PageRequest.of(0, 10);
        RawgGameDTO good = new RawgGameDTO(10, "Good", "d", "2020-01-01", List.of(), 80, List.of(), List.of(), List.of(), List.of());
        RawgGameDTO bad = new RawgGameDTO(11, "Bad", "d", "2020-01-01", List.of(), null, List.of(), List.of(), List.of(), List.of());
        RawgResponseDTO response = new RawgResponseDTO(List.of(good, bad), 2);
        when(rawgApiService.getGamesByMetacritic("asc", pageable)).thenReturn(response);
        when(mapper.ConvertRawgResponseToGamesModel(response)).thenReturn(new java.util.ArrayList<>(List.of(
                new GameSummaryDTO(10, "Good", "2020-01-01", 80, "", "", "desc", "dev", "pub", "", 0.0),
                new GameSummaryDTO(11, "Bad", "2020-01-01", null, "", "", "desc", "dev", "pub", "", 0.0)
        )));

        gameService.searchGamesByMetacritic("asc", pageable);

        // only good should be checked
        verify(gameService, times(1)).checkIfGameIsInDatabase("Good");
    }

    @Test
    void searchGameByDeveloper_savesMissingGamesToRepository() {
        PageRequest pageable = PageRequest.of(0, 10);
        RawgGameDTO r1 = new RawgGameDTO(21, "NewOne", "d", "2020-01-01", List.of(), 70, List.of(), List.of(), List.of(), List.of());
        RawgResponseDTO response = new RawgResponseDTO(List.of(r1), 1);
        when(rawgApiService.getGamesByDeveloper("DevName", pageable)).thenReturn(response);
        when(mapper.ConvertRawgResponseToGamesModel(response)).thenReturn(List.of(new GameSummaryDTO(21, "NewOne", "2020", 70, "", "", "desc", "dev", "pub", "", 0.0)));
        when(gameRepository.findBygameNameIgnoreCase("NewOne")).thenReturn(Optional.empty());

        gameService.searchGameByDeveloper("DevName", pageable);

        verify(gameRepository).save(any(GamesModel.class));
    }

    @Test
    void searchGameByDeveloper_doesNotSaveWhenGameAlreadyExists() {
        PageRequest pageable = PageRequest.of(0, 10);
        RawgGameDTO r1 = new RawgGameDTO(22, "Exists", "d", "2020-01-01", List.of(), 70, List.of(), List.of(), List.of(), List.of());
        RawgResponseDTO response = new RawgResponseDTO(List.of(r1), 1);
        when(rawgApiService.getGamesByDeveloper("Dev2", pageable)).thenReturn(response);
        when(mapper.ConvertRawgResponseToGamesModel(response)).thenReturn(List.of(new GameSummaryDTO(22, "Exists", "2020", 70, "", "", "desc", "dev", "pub", "", 0.0)));
        GamesModel existing = new GamesModel();
        existing.setGameName("Exists");
        when(gameRepository.findBygameNameIgnoreCase("Exists")).thenReturn(Optional.of(existing));

        gameService.searchGameByDeveloper("Dev2", pageable);

        verify(gameRepository, never()).save(argThat(g -> "Exists".equals(g.getGameName())==false));
    }
}




