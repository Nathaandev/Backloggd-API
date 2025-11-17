package com.example.backloggd.Controller;

import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
