package com.example.backloggd.Controller;

import com.example.backloggd.DTO.GameSummaryDTO;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    GameService gameService;

    @GetMapping("/search/{gameName}")
    public ResponseEntity<GamesModel> searchGame(@PathVariable String gameName) {
        return gameService.searchGame(gameName);
    }
    @GetMapping("/search")
    public ResponseEntity<Page<GameSummaryDTO>> searchGamesByGenre(@RequestParam String genres, Pageable pageable){
        Page<GameSummaryDTO> gamesPage = gameService.searchGameByGenre(genres, pageable);

        return ResponseEntity.ok(gamesPage);
    }
    @GetMapping("/search/dev")
    public ResponseEntity<Page<GameSummaryDTO>> searchGamesByDeveloper(@RequestParam String developer, Pageable pageable){
        Page<GameSummaryDTO> gamesPage = gameService.searchGameByDeveloper(developer, pageable);

        return ResponseEntity.ok(gamesPage);
    }
}
