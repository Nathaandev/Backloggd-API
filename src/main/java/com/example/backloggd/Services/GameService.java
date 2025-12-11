package com.example.backloggd.Services;


import java.util.List;

import com.example.backloggd.DTO.GameSummaryDTO;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.example.backloggd.DTO.ObjectsDTO.DevelopersDTO;
import com.example.backloggd.DTO.ObjectsDTO.GenreDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsWrapperDTO;
import com.example.backloggd.DTO.ObjectsDTO.PublishersDTO;
import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import com.example.backloggd.Models.GamesModel;
import com.example.backloggd.Repository.GameRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.backloggd.Util.GameDataMappers;

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
            List<GenreDTO> genres = bestMatch.genres();
            List<PlatformsWrapperDTO> platforms = bestMatch.platforms();
            GamesModel gameFound = new GamesModel();
            BeanUtils.copyProperties(bestMatch, gameFound);
            RawgGameDTO gameWithFullDetails = rawgApiService.GetGameDetailsWithID(gameFound.getRawgId());
            List<DevelopersDTO> developers = gameWithFullDetails.developers();
            List<PublishersDTO> publishers = gameWithFullDetails.publishers();

            GameDataMappers.ConsolidateGameData(gameFound, gameWithFullDetails, developers, genres, platforms, publishers );
            return ResponseEntity.status(HttpStatus.OK).body(gameRepository.save(gameFound));

        }
        var game = gamesModelOptional.get();
        return ResponseEntity.ok(game);
    }
    public Page<GameSummaryDTO> searchGameByGenre(String genres, Pageable pageable){
        RawgResponseDTO rawgResponse = rawgApiService.getGamesByGenre(genres, pageable);
        List<GameSummaryDTO> gamesFound = GameDataMappers.ConvertRawgResponseToGamesModel(rawgResponse);
        return new PageImpl<>(
                gamesFound,
                pageable,
                rawgResponse.count()
        );
    }
}