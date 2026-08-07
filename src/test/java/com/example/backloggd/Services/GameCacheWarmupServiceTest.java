package com.example.backloggd.Services;

import com.example.backloggd.DTO.GameSummaryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameCacheWarmupServiceTest {

    @Mock
    private GameService gameService;

    private GameCacheWarmupService warmupService;

    @BeforeEach
    void setUp() {
        warmupService = new GameCacheWarmupService(gameService);
    }

    @Test
    void warmupPopularGamesCache_callsGameServiceForAllPages() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<GameSummaryDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(gameService.getPopularGames(anyString(), any(Pageable.class))).thenReturn(emptyPage);

        warmupService.warmupPopularGamesCache();

        // Should call 9 times (pages 0-8)
        verify(gameService, times(9)).getPopularGames(eq("desc"), any(Pageable.class));
    }

    @Test
    void warmupPopularGamesCache_continuesOnError() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<GameSummaryDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(gameService.getPopularGames(anyString(), any(Pageable.class)))
            .thenThrow(new RuntimeException("API error"))
            .thenReturn(emptyPage);

        warmupService.warmupPopularGamesCache();

        // Should continue despite error on first page
        verify(gameService, times(9)).getPopularGames(eq("desc"), any(Pageable.class));
    }

    @Test
    void warmupPopularGenres_callsGameServiceForEachGenre() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<GameSummaryDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        when(gameService.searchGameByGenre(anyString(), any(Pageable.class))).thenReturn(emptyPage);

        warmupService.warmupPopularGenres();

        // Should call for 5 genres: Action, RPG, Adventure, Strategy, Shooter
        verify(gameService, times(5)).searchGameByGenre(anyString(), any(Pageable.class));
        verify(gameService).searchGameByGenre(eq("Action"), any(Pageable.class));
        verify(gameService).searchGameByGenre(eq("RPG"), any(Pageable.class));
        verify(gameService).searchGameByGenre(eq("Adventure"), any(Pageable.class));
        verify(gameService).searchGameByGenre(eq("Strategy"), any(Pageable.class));
        verify(gameService).searchGameByGenre(eq("Shooter"), any(Pageable.class));
    }

    @Test
    void warmupPopularGenres_continuesOnError() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<GameSummaryDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        when(gameService.searchGameByGenre(anyString(), any(Pageable.class)))
            .thenThrow(new RuntimeException("API error"))
            .thenReturn(emptyPage);

        warmupService.warmupPopularGenres();

        // Should continue despite error on first genre
        verify(gameService, times(5)).searchGameByGenre(anyString(), any(Pageable.class));
    }
}
