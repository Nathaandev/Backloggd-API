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
}
