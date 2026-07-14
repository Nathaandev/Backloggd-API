package com.example.backloggd.Controller;

import com.example.backloggd.DTO.GameResponseDTO;
import com.example.backloggd.DTO.GameSummaryDTO;
import com.example.backloggd.Services.GameService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/search/{gameName}")
    public ResponseEntity<GameResponseDTO> searchGame(@Valid @PathVariable String gameName) {
        return gameService.searchGame(gameName);
    }
    @GetMapping("/search/genre")
    public ResponseEntity<Page<GameSummaryDTO>> searchGamesByGenre(@Valid @RequestParam String genres, Pageable pageable){
        Page<GameSummaryDTO> gamesPage = gameService.searchGameByGenre(genres, pageable);

        return ResponseEntity.ok(gamesPage);
    }
    @GetMapping("/search/dev")
    public ResponseEntity<Page<GameSummaryDTO>> searchGamesByDeveloper(@Valid @RequestParam String developer, Pageable pageable){
        Page<GameSummaryDTO> gamesPage = gameService.searchGameByDeveloper(developer, pageable);

        return ResponseEntity.ok(gamesPage);
    }
    @GetMapping("/search/pub")
    public ResponseEntity<Page<GameSummaryDTO>> searchGamesByPublisher(@Valid @RequestParam String publisher, Pageable pageable){
        Page<GameSummaryDTO> gamesPage = gameService.searchGamesByPublishers(publisher, pageable);
        return ResponseEntity.ok(gamesPage);
    }

    @GetMapping("/search/metacritic")
    public ResponseEntity<Page<GameSummaryDTO>> searchGamesByMetacritic(@Valid @RequestParam String ordering, Pageable pageable){
        Page<GameSummaryDTO> gamesPage = gameService.searchGamesByMetacritic(ordering, pageable);
        return ResponseEntity.ok(gamesPage);
    }

    @GetMapping("/search/tags")
    public ResponseEntity<Page<GameSummaryDTO>> searchGamesByTags(@Valid @RequestParam String tags, Pageable pageable){
        Page<GameSummaryDTO> gamesPage = gameService.searchGamesByTags(tags, pageable);
        return ResponseEntity.ok(gamesPage);
    }
}
