package com.example.backloggd.Services;

import java.util.Optional;

import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    @Autowired
    GameRepository gameRepository;
    public ResponseEntity<GamesModel> searchGame(String gameName) {
        var gamesModelOptional = gameRepository.findByGameName(gameName);
        if(gamesModelOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var game = gamesModelOptional.get();
        return ResponseEntity.ok(game);
    }
}
