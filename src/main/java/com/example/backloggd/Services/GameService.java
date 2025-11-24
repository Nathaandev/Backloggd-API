package com.example.backloggd.Services;

import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Repository.GameRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    @Autowired
    GameRepository gameRepository;
    private final RawgApiService rawgApiService;

    public GameService(RawgApiService rawgApiService) {
        this.rawgApiService = rawgApiService;
    }

    public ResponseEntity<GamesModel> searchGame(String gameName) {
        var gamesModelOptional = gameRepository.findBygameNameIgnoreCase(gameName);
        if(gamesModelOptional.isEmpty()) {
            RawgResponseDTO rawgResponse = rawgApiService.getGames(gameName);
            if(rawgResponse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            var bestMatch = rawgResponse.results().get(0);
            GamesModel gameFound = new GamesModel();
            BeanUtils.copyProperties(bestMatch, gameFound);
            RawgGameDTO gameWithFullDetails = rawgApiService.GetGameDetailsWithID(gameFound.getRawgId());
            gameFound.setGameDescription(gameWithFullDetails.gameDescription());
            return ResponseEntity.status(HttpStatus.OK).body(gameRepository.save(gameFound));

        }
        var game = gamesModelOptional.get();
        return ResponseEntity.ok(game);

    }
}
