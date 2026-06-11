package com.example.backloggd.Services;

import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Repository.UserRepository;
import org.springframework.http.HttpStatusCode;
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
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final GameService gameService;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, GameService gameService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.gameService = gameService;
    }

    public ResponseEntity<UserModel> signUp(UserRegistrationDTO userRegistrationDTO){
        UserModel userModel = new UserModel(userRegistrationDTO.userName(), passwordEncoder.encode(userRegistrationDTO.password()), userRegistrationDTO.userEmail());
        return ResponseEntity.ok(userRepository.save(userModel));
    }
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserModel user = userRepository.findByUserName(userName);
    return User.withUsername(user.getUserName())
            .password(user.getPassword())
            .roles("USER")
            .build();
    }

    public ResponseEntity<String> addGameToWishlist(String gameName, Authentication authentication){
        var gamesModel = (gameService.searchGame(gameName));
        UserModel user = userRepository.findByUserName(authentication.getName());
        user.getWishlist().add(gamesModel.getBody());
        userRepository.save(user);
        return ResponseEntity.ok("Game added to wishlist.");
    }
    public ResponseEntity<List<String>> getUserWishlist(Authentication authentication) {
        UserModel user = userRepository.findByUserName(authentication.getName());
        List<String> gameNames = new ArrayList<>();
        for (GamesModel game : user.getWishlist()) {
            gameNames.add(game.getGameName());
        }
        return ResponseEntity.ok(gameNames);
    }


}