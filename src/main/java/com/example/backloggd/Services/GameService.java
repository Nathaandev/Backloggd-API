package com.example.backloggd.Services;

import java.util.List;
import java.util.stream.Collectors;

import com.example.backloggd.DTO.ObjectsDTO.DevelopersDTO;
import com.example.backloggd.DTO.ObjectsDTO.GenreDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsWrapperDTO;
import com.example.backloggd.DTO.ObjectsDTO.PublishersDTO;
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
            List<GenreDTO> genres = bestMatch.genres();

            List<PlatformsWrapperDTO> platforms = bestMatch.platforms();
            String genresString = genres.stream().map(GenreDTO::name).collect(Collectors.joining(", "));
            GamesModel gameFound = new GamesModel();
            BeanUtils.copyProperties(bestMatch, gameFound);
            RawgGameDTO gameWithFullDetails = rawgApiService.GetGameDetailsWithID(gameFound.getRawgId());
            List<DevelopersDTO> developers = gameWithFullDetails.developers();
            List<PublishersDTO> publishers = gameWithFullDetails.publishers();

            String platformsString = platforms.stream().map(PlatformsWrapperDTO::platforms)
                                              .map(PlatformsDTO::name).collect(Collectors.joining(", "));
            String publishersString = publishers.stream().map(PublishersDTO::name).collect(Collectors.joining(", "));
            String developersString = developers.stream().map(DevelopersDTO::name).collect(Collectors.joining(", "));

            gameFound.setPlatforms(platformsString);
            gameFound.setPublishers(publishersString);
            gameFound.setGenres(genresString);
            gameFound.setGameDescription(gameWithFullDetails.gameDescription());
            gameFound.setDevelopers(developersString);
            return ResponseEntity.status(HttpStatus.OK).body(gameRepository.save(gameFound));

        }
        var game = gamesModelOptional.get();
        return ResponseEntity.ok(game);

    }
}