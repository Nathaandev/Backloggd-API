package com.example.backloggd.Services;

import com.example.backloggd.Models.ReviewModel;
import com.example.backloggd.Repository.GameRepository;
import com.example.backloggd.Repository.ReviewRepository;
import com.example.backloggd.Util.GameDataMappers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private GameService gameService;

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
}
