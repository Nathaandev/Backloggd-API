package com.example.backloggd.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GameCacheWarmupService {

    private static final Logger logger = LoggerFactory.getLogger(GameCacheWarmupService.class);
    
    private final GameService gameService;

    public GameCacheWarmupService(GameService gameService) {
        this.gameService = gameService;
    }

    @Scheduled(cron = "0 0 3 * * ?") // Runs daily at 3 AM
    public void warmupPopularGamesCache() {
        logger.info("Starting cache warmup for popular games");
        
        try {
            // Pre-load top 100 games by metacritic rating (descending)
            int totalPages = 9; // 10 pages of 10 games each = 100 games
            int pageSize = 10;
            
            for (int page = 0; page <= totalPages; page++) {
                Pageable pageable = PageRequest.of(page, pageSize);
                logger.info("Warming up page {} of popular games", page + 1);
                
                try {
                    gameService.searchGamesByMetacritic("desc", pageable);
                } catch (Exception e) {
                    logger.error("Error warming up page {}: {}", page + 1, e.getMessage());
                }
            }
            
            logger.info("Cache warmup completed successfully");
        } catch (Exception e) {
            logger.error("Error during cache warmup: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 */6 * * ?") // Runs every 6 hours
    public void warmupPopularGenres() {
        logger.info("Starting cache warmup for popular genres");
        
        String[] popularGenres = {"Action", "RPG", "Adventure", "Strategy", "Shooter"};
        
        for (String genre : popularGenres) {
            try {
                Pageable pageable = PageRequest.of(0, 12);
                logger.info("Warming up genre: {}", genre);
                gameService.searchGameByGenre(genre, pageable);
            } catch (Exception e) {
                logger.error("Error warming up genre {}: {}", genre, e.getMessage());
            }
        }
        
        logger.info("Genre cache warmup completed");
    }
}
