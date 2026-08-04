package com.example.backloggd.Services;

import com.example.backloggd.DTO.ReviewSummaryDTO;
import com.example.backloggd.DTO.UserProfileDTO;
import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Exceptions.UsernameAlreadyInUseException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameService gameService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void signUpRejectsDuplicateUsernames() {
        UserRegistrationDTO request = new UserRegistrationDTO("jane", "secret123");
        when(userRepository.findByUserName("jane")).thenReturn(new UserModel("jane", "encoded"));

        assertThrows(UsernameAlreadyInUseException.class, () -> userService.signUp(request));
    }

    @Test
    void getProfileReturnsProfileWithUserReviews() {
        Authentication authentication = new TestingAuthenticationToken("jane", null);
        UserModel user = new UserModel("jane", "encoded");
        ReviewModel review = new ReviewModel();
        review.setGameName("Hades");
        review.setRating(4.5f);
        review.setReview("Great game");
        review.setGameTime(35);

        when(userRepository.findByUserName("jane")).thenReturn(user);
        when(reviewRepository.findByUserModelUserName("jane")).thenReturn(List.of(review));

        ResponseEntity<UserProfileDTO> response = userService.getProfile(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("jane", response.getBody().userName());
        assertEquals(1, response.getBody().reviews().size());
        ReviewSummaryDTO summary = response.getBody().reviews().get(0);
        assertEquals("Hades", summary.gameName());
        assertEquals(4.5f, summary.rating());
        assertEquals("Great game", summary.review());
        assertEquals(35, summary.gameTime());
    }

    @Test
    void addGameToWishlistAddsGameToUserWishlist() {
        Authentication authentication = new TestingAuthenticationToken("jane", null);
        UserModel user = new UserModel("jane", "encoded");
        user.setWishlist(new ArrayList<>());
        com.example.backloggd.Models.GamesModel game = new com.example.backloggd.Models.GamesModel();
        game.setGameName("Hades");

        when(userRepository.findByUserName("jane")).thenReturn(user);
        when(gameRepository.findBygameNameIgnoreCase("Hades")).thenReturn(java.util.Optional.of(game));
        when(gameService.checkIfGameIsInDatabase("Hades")).thenReturn(ResponseEntity.ok("Game found in database."));

        ResponseEntity<String> response = userService.addGameToWishlist("Hades", authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Game added to wishlist.", response.getBody());
        assertEquals(1, user.getWishlist().size());
        assertEquals("Hades", user.getWishlist().get(0).getGameName());
    }
}
