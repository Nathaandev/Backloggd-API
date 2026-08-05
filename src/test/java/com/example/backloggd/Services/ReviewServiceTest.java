package com.example.backloggd.Services;

import com.example.backloggd.DTO.GameReviewDTO;
import com.example.backloggd.DTO.ReviewResponseDTO;
import com.example.backloggd.Exceptions.AlreadyPublishedAReviewException;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Models.ReviewModel;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Repository.GameRepository;
import com.example.backloggd.Repository.ReviewRepository;
import com.example.backloggd.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private GameService gameService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void publishReviewsPersistsReviewWithGameAndUserInformation() {
        GameReviewDTO request = new GameReviewDTO(4.5f, "Great game", 40);
        Authentication authentication = new TestingAuthenticationToken("jane", null);
        GamesModel game = new GamesModel();
        game.setGameName("Hades");
        UserModel user = new UserModel("jane", "encoded");
        ReviewModel savedReview = new ReviewModel();
        savedReview.setReview("Great game");
        savedReview.setRating(4.5f);
        savedReview.setGameTime(40);
        savedReview.setGameName("Hades");
        savedReview.setUserName("jane");

        when(gameService.checkIfGameIsInDatabase("Hades")).thenReturn(ResponseEntity.ok("Game found in database."));
        when(gameRepository.findBygameNameIgnoreCase("Hades")).thenReturn(Optional.of(game));
        when(userRepository.findByUserName("jane")).thenReturn(user);
        when(reviewRepository.findByGameGameNameAndUserModelUserName("Hades", "jane")).thenReturn(Optional.empty());
        when(reviewRepository.save(org.mockito.ArgumentMatchers.any(ReviewModel.class))).thenReturn(savedReview);

        ResponseEntity<ReviewResponseDTO> response = reviewService.publishReviews("Hades", authentication, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Hades", response.getBody().gameName());
        assertEquals("jane", response.getBody().userName());
        assertEquals(4.5f, response.getBody().rating());

        ArgumentCaptor<ReviewModel> captor = ArgumentCaptor.forClass(ReviewModel.class);
        verify(reviewRepository).save(captor.capture());
        ReviewModel captured = captor.getValue();
        assertEquals("Hades", captured.getGameName());
        assertEquals("jane", captured.getUserName());
    }

    @Test
    void publishReviewsRejectsDuplicateReviewForSameGameAndUser() {
        GameReviewDTO request = new GameReviewDTO(4.0f, "Good title", 20);
        Authentication authentication = new TestingAuthenticationToken("jane", null);
        GamesModel game = new GamesModel();
        game.setGameName("Hades");
        UserModel user = new UserModel("jane", "encoded");

        when(gameService.checkIfGameIsInDatabase("Hades")).thenReturn(ResponseEntity.ok("Game found in database."));
        when(gameRepository.findBygameNameIgnoreCase("Hades")).thenReturn(Optional.of(game));
        when(userRepository.findByUserName("jane")).thenReturn(user);
        when(reviewRepository.findByGameGameNameAndUserModelUserName("Hades", "jane")).thenReturn(Optional.of(new ReviewModel()));

        assertThrows(AlreadyPublishedAReviewException.class,
                () -> reviewService.publishReviews("Hades", authentication, request));
    }

    // publishReviews validation & propagation tests
    @Test
    void publishReviews_throwsWhenAuthenticationNullOrBlank() {
        GameReviewDTO request = new GameReviewDTO(4.0f, "ok", 10);
        Authentication nullAuth = null;
        Authentication blankAuth = new TestingAuthenticationToken("   ", null);

        assertThrows(IllegalArgumentException.class, () -> reviewService.publishReviews("Hades", nullAuth, request));
        assertThrows(IllegalArgumentException.class, () -> reviewService.publishReviews("Hades", blankAuth, request));
    }

    @Test
    void publishReviews_throwsWhenGameNameNullOrBlank() {
        GameReviewDTO request = new GameReviewDTO(4.0f, "ok", 10);
        Authentication auth = new TestingAuthenticationToken("jane", null);

        assertThrows(IllegalArgumentException.class, () -> reviewService.publishReviews(null, auth, request));
        assertThrows(IllegalArgumentException.class, () -> reviewService.publishReviews("   ", auth, request));
    }

    @Test
    void publishReviews_throwsWhenGameReviewDtoNull() {
        Authentication auth = new TestingAuthenticationToken("jane", null);
        assertThrows(IllegalArgumentException.class, () -> reviewService.publishReviews("Hades", auth, null));
    }

    @Test
    void publishReviews_throwsWhenGameNotInRepository() {
        GameReviewDTO request = new GameReviewDTO(4.0f, "ok", 10);
        Authentication auth = new TestingAuthenticationToken("jane", null);

        when(gameService.checkIfGameIsInDatabase("Hades")).thenReturn(ResponseEntity.ok("Game found in database."));
        when(gameRepository.findBygameNameIgnoreCase("Hades")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> reviewService.publishReviews("Hades", auth, request));
    }

    @Test
    void publishReviews_throwsWhenUserNotFound() {
        GameReviewDTO request = new GameReviewDTO(4.0f, "ok", 10);
        Authentication auth = new TestingAuthenticationToken("ghost", null);
        GamesModel game = new GamesModel();
        game.setGameName("Hades");

        when(gameService.checkIfGameIsInDatabase("Hades")).thenReturn(ResponseEntity.ok("Game found in database."));
        when(gameRepository.findBygameNameIgnoreCase("Hades")).thenReturn(Optional.of(game));
        when(userRepository.findByUserName("ghost")).thenReturn(null);

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> reviewService.publishReviews("Hades", auth, request));
    }

    @Test
    void publishReviews_propagatesWhenGameServiceThrows() {
        GameReviewDTO request = new GameReviewDTO(4.0f, "ok", 10);
        Authentication auth = new TestingAuthenticationToken("jane", null);

        when(gameService.checkIfGameIsInDatabase("Hades")).thenThrow(new RuntimeException("rawg failure"));

        assertThrows(RuntimeException.class, () -> reviewService.publishReviews("Hades", auth, request));
    }
}
