package com.example.backloggd.Services;


import java.util.Iterator;import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.backloggd.Util.GameDataMappers;

@Service
public class GameService {
    @Autowired
    GameRepository gameRepository;
    private final RawgApiService rawgApiService;

    private final GameDataMappers mapper;

    public GameService(RawgApiService rawgApiService, GameDataMappers mapper){
        this.rawgApiService = rawgApiService;
        this.mapper = mapper;
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
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(rawgResponse);

        //Check if the game is in the database, if it isn't, saves i1t.
        for (GameSummaryDTO gameSummaryDTO : gamesFound){
            Optional<GamesModel> gameOptional = gameRepository.findBygameNameIgnoreCase(gameSummaryDTO.gameName());
            if (gameOptional.isEmpty()){
                GamesModel game = new GamesModel();
                BeanUtils.copyProperties(gameSummaryDTO, game);
                gameRepository.save(game);
            }
        }

        return new PageImpl<>(
                gamesFound,
                pageable,
                rawgResponse.count()
        );
    }
    public Page<GameSummaryDTO> searchGameByDeveloper(String developer, Pageable pageable){
        RawgResponseDTO rawgResponse = rawgApiService.getGamesByDeveloper(developer, pageable);
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(rawgResponse);

        for (GameSummaryDTO gameSummaryDTO : gamesFound){
            Optional<GamesModel> gameOptional = gameRepository.findBygameNameIgnoreCase(gameSummaryDTO.gameName());
            if (gameOptional.isEmpty()){
                GamesModel game = new GamesModel();
                BeanUtils.copyProperties(gameSummaryDTO, game);
                gameRepository.save(game);
            }
        }
        return new PageImpl<>(
                gamesFound,
                pageable,
                rawgResponse.count()
        );
    }

    public Page<GameSummaryDTO> searchGamesByPublishers(String publisher, Pageable pageable){
        RawgResponseDTO rawgResponse = rawgApiService.getGamesByPublishers(publisher, pageable);
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(rawgResponse);

        for (GameSummaryDTO gameSummaryDTO : gamesFound){
            Optional<GamesModel> gameOptional = gameRepository.findBygameNameIgnoreCase(gameSummaryDTO.gameName());
            if (gameOptional.isEmpty()){
                GamesModel game = new GamesModel();
                BeanUtils.copyProperties(gameSummaryDTO, game);
                gameRepository.save(game);
            }
        }

        return new PageImpl<>(
                gamesFound,
                pageable,
                rawgResponse.count()
        );


    }

    public Page<GameSummaryDTO> searchGamesByMetacritic(String ordering, Pageable pageable){
        RawgResponseDTO rawgResponse = rawgApiService.getGamesByMetacritic(ordering, pageable);
        List<GameSummaryDTO> gamesFound = mapper.ConvertRawgResponseToGamesModel(rawgResponse);
        for (GameSummaryDTO gameSummaryDTO : gamesFound){
            if (gameSummaryDTO.metacritic() == null){
                gamesFound.remove(gameSummaryDTO);
            }
            Optional<GamesModel> gameOptional = gameRepository.findBygameNameIgnoreCase(gameSummaryDTO.gameName());
            if (gameOptional.isEmpty()){
                GamesModel game = new GamesModel();
                BeanUtils.copyProperties(gameSummaryDTO, game);
                gameRepository.save(game);
            }

        }

        return new PageImpl<>(
                gamesFound,
                pageable,
                rawgResponse.count()
        );
    }
}