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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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

    @Test
    void addGameToWishlist_throwsWhenAuthenticationNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> userService.addGameToWishlist("Hades", null));
        Authentication authBlank = new TestingAuthenticationToken("   ", null);
        assertThrows(IllegalArgumentException.class, () -> userService.addGameToWishlist("Hades", authBlank));
    }

    @Test
    void addGameToWishlist_throwsWhenGameNameNullOrBlank() {
        Authentication auth = new TestingAuthenticationToken("jane", null);
        assertThrows(IllegalArgumentException.class, () -> userService.addGameToWishlist(null, auth));
        assertThrows(IllegalArgumentException.class, () -> userService.addGameToWishlist("   ", auth));
    }

    @Test
    void addGameToWishlist_throwsWhenGameNotFound() {
        Authentication auth = new TestingAuthenticationToken("jane", null);
        UserModel user = new UserModel("jane", "encoded");
        when(userRepository.findByUserName("jane")).thenReturn(user);
        when(gameRepository.findBygameNameIgnoreCase("Hades")).thenReturn(java.util.Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.addGameToWishlist("Hades", auth));
    }

    @Test
    void addGameToWishlist_throwsWhenUserNotFound() {
        Authentication auth = new TestingAuthenticationToken("jane", null);
        when(userRepository.findByUserName("jane")).thenReturn(null);
        com.example.backloggd.Models.GamesModel game = new com.example.backloggd.Models.GamesModel();
        game.setGameName("Hades");
        when(gameRepository.findBygameNameIgnoreCase("Hades")).thenReturn(java.util.Optional.of(game));
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () -> userService.addGameToWishlist("Hades", auth));
    }

    @Test
    void addGameToWishlist_handlesNullWishlistByInitializing() {
        Authentication auth = new TestingAuthenticationToken("jane", null);
        UserModel user = new UserModel("jane", "encoded");
        user.setWishlist(null);
        com.example.backloggd.Models.GamesModel game = new com.example.backloggd.Models.GamesModel();
        game.setGameName("Hades");
        when(userRepository.findByUserName("jane")).thenReturn(user);
        when(gameRepository.findBygameNameIgnoreCase("Hades")).thenReturn(java.util.Optional.of(game));
        when(gameService.checkIfGameIsInDatabase("Hades")).thenReturn(ResponseEntity.ok("ok"));
        ResponseEntity<String> resp = userService.addGameToWishlist("Hades", auth);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1, user.getWishlist().size());
    }

    @Test
    void removeGameFromWishlistRemovesExistingGame() {
        Authentication authentication = new TestingAuthenticationToken("jane", null);
        UserModel user = new UserModel("jane", "encoded");
        com.example.backloggd.Models.GamesModel game = new com.example.backloggd.Models.GamesModel();
        game.setGameName("Hades");
        user.setWishlist(new ArrayList<>(List.of(game)));

        when(userRepository.findByUserName("jane")).thenReturn(user);
        when(gameRepository.findBygameNameIgnoreCase("Hades")).thenReturn(java.util.Optional.of(game));

        ResponseEntity<String> response = userService.removeGameFromWishlist("Hades", authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Game deleted.", response.getBody());
        assertEquals(0, user.getWishlist().size());
    }

    // getUserWishlist tests
    @Test
    void getUserWishlist_successReturnsNames() {
        Authentication auth = new TestingAuthenticationToken("jane", null);
        UserModel user = new UserModel("jane", "encoded");
        com.example.backloggd.Models.GamesModel g = new com.example.backloggd.Models.GamesModel();
        g.setGameName("Hades");
        user.setWishlist(new ArrayList<>(List.of(g)));
        when(userRepository.findByUserName("jane")).thenReturn(user);

        ResponseEntity<java.util.List<String>> resp = userService.getUserWishlist(auth);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1, resp.getBody().size());
        assertEquals("Hades", resp.getBody().get(0));
    }

    @Test
    void getUserWishlist_throwsWhenAuthenticationNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> userService.getUserWishlist(null));
        Authentication authBlank = new TestingAuthenticationToken("   ", null);
        assertThrows(IllegalArgumentException.class, () -> userService.getUserWishlist(authBlank));
    }

    @Test
    void getUserWishlist_throwsWhenUserNotFound() {
        Authentication auth = new TestingAuthenticationToken("jane", null);
        when(userRepository.findByUserName("jane")).thenReturn(null);
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () -> userService.getUserWishlist(auth));
    }

    @Test
    void getUserWishlist_returnsEmptyWhenWishlistNull() {
        Authentication auth = new TestingAuthenticationToken("jane", null);
        UserModel user = new UserModel("jane", "encoded");
        user.setWishlist(null);
        when(userRepository.findByUserName("jane")).thenReturn(user);
        ResponseEntity<java.util.List<String>> resp = userService.getUserWishlist(auth);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(0, resp.getBody().size());
    }

    // signUp additional tests
    @Test
    void signUpSuccessReturnsSavedUser() {
        // prepare
        when(userRepository.findByUserName("alice")).thenReturn(null);
        when(userRepository.save(any(UserModel.class))).thenAnswer(inv -> inv.getArgument(0));

        // inject a real PasswordEncoder behavior via mock
        when(userRepository.findByUserName("alice")).thenReturn(null);
        // create service with mocked password encoder
        var service = new UserService(org.mockito.Mockito.mock(org.springframework.security.crypto.password.PasswordEncoder.class), userRepository, gameService, gameRepository, reviewRepository);

        UserRegistrationDTO dto = new UserRegistrationDTO("alice", "pw");
        assertDoesNotThrow(() -> service.signUp(dto));
    }

    @Test
    void signUp_throwsWhenDtoNull() {
        assertThrows(IllegalArgumentException.class, () -> userService.signUp(null));
    }

    @Test
    void signUp_throwsWhenUsernameNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> userService.signUp(new UserRegistrationDTO(null, "p")));
        assertThrows(IllegalArgumentException.class, () -> userService.signUp(new UserRegistrationDTO("   ", "p")));
    }

    @Test
    void signUp_throwsWhenPasswordNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> userService.signUp(new UserRegistrationDTO("bob", null)));
        assertThrows(IllegalArgumentException.class, () -> userService.signUp(new UserRegistrationDTO("bob", "   ")));
    }

    // loadUserByUsername tests
    @Test
    void loadUserByUsername_successReturnsUserDetails() {
        when(userRepository.findByUserName("jane")).thenReturn(new UserModel("jane", "encoded"));
        var details = userService.loadUserByUsername("jane");
        assertEquals("jane", details.getUsername());
    }

    @Test
    void loadUserByUsername_throwsWhenUsernameNullOrBlank() {
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () -> userService.loadUserByUsername(null));
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () -> userService.loadUserByUsername("   "));
    }

    @Test
    void loadUserByUsername_throwsWhenUserNotFound() {
        when(userRepository.findByUserName("foo")).thenReturn(null);
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () -> userService.loadUserByUsername("foo"));
    }
}
