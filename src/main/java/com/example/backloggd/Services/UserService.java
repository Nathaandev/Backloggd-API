package com.example.backloggd.Services;

import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Exceptions.UsernameAlreadyInUseException;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Repository.GameRepository;
import com.example.backloggd.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class UserService implements UserDetailsService {

    Logger logger = LoggerFactory.getLogger(UserService.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final GameService gameService;
    private final GameRepository gameRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, GameService gameService, GameRepository gameRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.gameService = gameService;
        this.gameRepository = gameRepository;
    }

    public ResponseEntity<UserModel> signUp(UserRegistrationDTO userRegistrationDTO){
        if (userRegistrationDTO == null) {
            throw new IllegalArgumentException("User registration is required.");
        }
        if (userRegistrationDTO.userName() == null || userRegistrationDTO.userName().isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (userRegistrationDTO.password() == null || userRegistrationDTO.password().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (userRepository.findByUserName(userRegistrationDTO.userName()) != null) {
            logger.error("Username {} is already in use.", userRegistrationDTO.userName());
            throw new UsernameAlreadyInUseException("Username is already in use.");
        }
        UserModel userModel = new UserModel(userRegistrationDTO.userName(), passwordEncoder.encode(userRegistrationDTO.password()));
        logger.info("User {} registered successfully.", userRegistrationDTO.userName());
        return ResponseEntity.ok(userRepository.save(userModel));
    }
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        if (userName == null || userName.isBlank()) {
            throw new UsernameNotFoundException("Username is required.");
        }
        UserModel user = userRepository.findByUserName(userName);
        if (user == null) {
            throw new UsernameNotFoundException("User not found.");
        }
        logger.info("Loading user details for {}.", userName);
        return User.withUsername(user.getUserName())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public ResponseEntity<String> addGameToWishlist(String gameName, Authentication authentication){
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authentication is required.");
        }
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Game name is required.");
        }
        gameService.checkIfGameIsInDatabase(gameName);
        var gamesModel = gameRepository.findBygameNameIgnoreCase(gameName);
        var game = gamesModel.orElseThrow(() -> new IllegalArgumentException("Game not found."));
        UserModel user = userRepository.findByUserName(authentication.getName());
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (user.getWishlist() == null) {
            user.setWishlist(new ArrayList<>());
        }
        user.getWishlist().add(game);
        userRepository.save(user);
        logger.info("{} was added to  {}'s wishlist.", gameName, authentication.getName());
        return ResponseEntity.ok("Game added to wishlist.");
    }
    public ResponseEntity<List<String>> getUserWishlist(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authentication is required.");
        }
        UserModel user = userRepository.findByUserName(authentication.getName());
        if (user == null) {
            logger.warn("User {} not found when trying to retrieve wishlist.", authentication.getName());
            return ResponseEntity.notFound().build();
        }
        if (user.getWishlist() == null) {
            return ResponseEntity.ok(List.of());
        }
        List<String> gameNames = new ArrayList<>();
        for (GamesModel game : user.getWishlist()) {
            gameNames.add(game.getGameName());
        }
        return ResponseEntity.ok(gameNames);
    }

    public ResponseEntity<String> removeGameFromWishlist(String gameName, Authentication authentication){
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authentication is required.");
        }
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Game name is required.");
        }
        var gamesModel = gameRepository.findBygameNameIgnoreCase(gameName);
        UserModel user =  userRepository.findByUserName(authentication.getName());
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (user.getWishlist() == null) {
            user.setWishlist(new ArrayList<>());
        }
        user.getWishlist().remove(gamesModel.orElseThrow(() -> new IllegalArgumentException("Game not found.")));
        userRepository.save(user);
        logger.info("{} was removed from  {}'s wishlist.", gameName, authentication.getName());
        return ResponseEntity.ok("Game deleted.");
    }


}