package com.example.backloggd.Services;

import com.example.backloggd.DTO.GameReviewDTO;
import com.example.backloggd.DTO.ReviewResponseDTO;
import com.example.backloggd.Exceptions.AlreadyPublishedAReviewException;
import com.example.backloggd.Models.ReviewModel;
import com.example.backloggd.Repository.GameRepository;
import com.example.backloggd.Repository.ReviewRepository;
import com.example.backloggd.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class ReviewService {

    Logger logger = LoggerFactory.getLogger(ReviewService.class);

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
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authentication is required.");
        }
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Game name is required.");
        }
        if (gameReviewDTO == null) {
            throw new IllegalArgumentException("Review data is required.");
        }
        gameService.checkIfGameIsInDatabase(gameName);
        var gamesModelOptional = gameRepository.findBygameNameIgnoreCase(gameName);
        var game = gamesModelOptional.orElseThrow(() -> new IllegalArgumentException("Game not found."));
        var user = userRepository.findByUserName(authentication.getName());
        if (user == null) {
            throw new UsernameNotFoundException("User not found.");
        }
        if (reviewRepository.findByGameGameNameAndUserModelUserName(gameName, authentication.getName()).isPresent()) {
            logger.error("User {} has already published a review for {}.", authentication.getName(), gameName);
            throw new AlreadyPublishedAReviewException("You have already published a review for this game.");
        }
        ReviewModel reviewModel = new ReviewModel();
        BeanUtils.copyProperties(gameReviewDTO, reviewModel);
        reviewModel.setGameName(gameName);
        reviewModel.setUserName(authentication.getName());
        reviewModel.setGame(game);
        reviewModel.setUserModel(user);
        var saved = reviewRepository.save(reviewModel);
        ReviewResponseDTO reviewResponseDTO = new ReviewResponseDTO(
                game.getGameName(),
                authentication.getName(),
                saved.getReview(),
                saved.getRating(),
                saved.getGameTime());
        logger.info("Review published for {} by {}.", gameName, authentication.getName());

        return ResponseEntity.ok(reviewResponseDTO);
    }

    public ResponseEntity<String> deleteReview(String gameName, Authentication authentication){
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authentication is required.");
        }
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Game name is required.");
        }
        var review = reviewRepository.findByGameGameNameAndUserModelUserName(gameName, authentication.getName());
        if (review.isEmpty()) {
            logger.warn("No review found for game {} by user {}.", gameName, authentication.getName());
            throw new IllegalArgumentException("Review not found.");
        }
        reviewRepository.delete(review.get());
        logger.info("Review deleted for {} by {}.", gameName, authentication.getName());
        return ResponseEntity.ok("Review deleted successfully.");
    }

    public ResponseEntity<ReviewResponseDTO> updateReview(String gameName, Authentication authentication, GameReviewDTO gameReviewDTO){
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalArgumentException("Authentication is required.");
        }
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("Game name is required.");
        }
        if (gameReviewDTO == null) {
            throw new IllegalArgumentException("Review data is required.");
        }
        var review = reviewRepository.findByGameGameNameAndUserModelUserName(gameName, authentication.getName());
        if (review.isEmpty()) {
            logger.warn("No review found for game {} by user {}.", gameName, authentication.getName());
            throw new IllegalArgumentException("Review not found.");
        }
        ReviewModel reviewModel = review.get();
        reviewModel.setRating(gameReviewDTO.rating());
        reviewModel.setReview(gameReviewDTO.review());
        reviewModel.setGameTime(gameReviewDTO.gameTime());
        var updated = reviewRepository.save(reviewModel);
        ReviewResponseDTO reviewResponseDTO = new ReviewResponseDTO(
                updated.getGameName(),
                authentication.getName(),
                updated.getReview(),
                updated.getRating(),
                updated.getGameTime());
        logger.info("Review updated for {} by {}.", gameName, authentication.getName());
        return ResponseEntity.ok(reviewResponseDTO);
    }
}
