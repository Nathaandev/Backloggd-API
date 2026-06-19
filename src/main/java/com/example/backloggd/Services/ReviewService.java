package com.example.backloggd.Services;

import com.example.backloggd.DTO.GameReviewDTO;
import com.example.backloggd.DTO.ReviewResponseDTO;
import com.example.backloggd.Models.ReviewModel;
import com.example.backloggd.Repository.GameRepository;
import com.example.backloggd.Repository.ReviewRepository;
import com.example.backloggd.Repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final GameService gameService;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public ReviewService(GameService gameService, GameRepository gameRepository, UserRepository userRepository, ReviewRepository reviewRepository) {
        this.gameService = gameService;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    public ResponseEntity<ReviewResponseDTO> publishReviews(String gameName, Authentication authentication, GameReviewDTO gameReviewDTO){
        gameService.checkIfGameIsInDatabase(gameName);
        var gamesModelOptional = gameRepository.findBygameNameIgnoreCase(gameName);
        var game = gamesModelOptional.get();
        var user = userRepository.findByUserName(authentication.getName());
        ReviewModel reviewModel = new ReviewModel();
        BeanUtils.copyProperties(gameReviewDTO, reviewModel);
        reviewModel.setGame(game);
        reviewModel.setUserModel(user);
        reviewModel.setGameName(game.getGameName());
        reviewModel.setUserName(user.getUserName());
        var saved = reviewRepository.save(reviewModel);
        ReviewResponseDTO reviewResponseDTO = new ReviewResponseDTO(
                game.getGameName(),
                authentication.getName(),
                saved.getReview(),
                saved.getRating(),
                saved.getGameTime());

        return ResponseEntity.ok(reviewResponseDTO);
    }
}
