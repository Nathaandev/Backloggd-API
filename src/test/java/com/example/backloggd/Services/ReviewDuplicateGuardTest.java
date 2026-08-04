package com.example.backloggd.Services;

import com.example.backloggd.DTO.GameReviewDTO;
import com.example.backloggd.Exceptions.AlreadyPublishedAReviewException;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Models.ReviewModel;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Repository.GameRepository;
import com.example.backloggd.Repository.ReviewRepository;
import com.example.backloggd.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewDuplicateGuardTest {

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
}
