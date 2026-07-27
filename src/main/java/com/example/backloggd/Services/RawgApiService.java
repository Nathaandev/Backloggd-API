package com.example.backloggd.Services;

import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Service
public class RawgApiService {

    Logger logger = LoggerFactory.getLogger(RawgApiService.class);

    @Value("${rawg.api.base-url}")
    private String baseUrl;

    @Value("${rawg.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public RawgApiService(
            @Value("${rawg.api.base-url}") String baseUrl,
            @Value("${rawg.api.key}") String apiKey
    ){
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }
    public RawgResponseDTO getGames(String gameName){
        validateQueryText(gameName, "Game name");
        return executeRequest(webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/games")
                       .queryParam("search", gameName)
                       .queryParam("key", apiKey)
                       .build()), RawgResponseDTO.class, "Fetching games for " + gameName);
    }
    public RawgGameDTO GetGameDetailsWithID(Integer rawgId){
        if (rawgId == null) {
            throw new IllegalArgumentException("RAWG game id is required.");
        }
        return executeRequest(webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/games/{id}")
                                            .queryParam("key", apiKey)
                                            .build(rawgId)), RawgGameDTO.class, "Fetching game details for ID " + rawgId);
    }
    public RawgResponseDTO getGamesByGenre(String genres, Pageable pageable){
        validateQueryText(genres, "Genre");
        int rawgPageNumber = pageable.getPageNumber() + 1;

        return executeRequest(webClient.get()
                       .uri(uriBuilder -> uriBuilder.path("/games")
                                                    .queryParam("genres", genres)
                                                    .queryParam("key", apiKey)
                                                    //Set the page size to 20
                                                    .queryParam("page_size", "20")
                                                    .queryParam("page", rawgPageNumber)
                                                    .build()), RawgResponseDTO.class, "Fetching games by genre " + genres);
    }
    public RawgResponseDTO getGamesByDeveloper(String developer, Pageable pageable){
        validateQueryText(developer, "Developer");
        int rawgPageNumber = pageable.getPageNumber() + 1;

        return executeRequest(webClient.get()
                       .uri(uriBuilder -> uriBuilder.path("/games")
                                                    .queryParam("developers", developer)
                                                    .queryParam("key", apiKey)
                                                    //Set the page size to 20
                                                    .queryParam("page_size", "20")
                                                    .queryParam("page", rawgPageNumber)
                                                    .build()), RawgResponseDTO.class, "Fetching games by developer " + developer);
    }
    public RawgResponseDTO getGamesByPublishers(String publisher, Pageable pageable){
        validateQueryText(publisher, "Publisher");
        int rawgPageNumber = pageable.getPageNumber() + 1;

        return executeRequest(webClient.get()
                       .uri(uriBuilder -> uriBuilder.path("/games")
                                                    .queryParam("publishers", publisher)
                                                    .queryParam("key", apiKey)
                                                    //Set the page size to 20
                                                    .queryParam("page_size", "20")
                                                    .queryParam("page", rawgPageNumber)
                                                    .build()), RawgResponseDTO.class, "Fetching games by publisher " + publisher);

    }
    public RawgResponseDTO getGamesByMetacritic(String ordering, Pageable pageable){
        validateQueryText(ordering, "Ordering");
        int rawgPageNumber = pageable.getPageNumber() + 1;

        return executeRequest(webClient.get()
                       .uri(uriBuilder -> uriBuilder.path("/games")
                                .queryParam("ordering", ordering )
                                .queryParam("key", apiKey)
                                .queryParam("page_size", "20")
                                .queryParam("page", rawgPageNumber)
                                .build()), RawgResponseDTO.class, "Fetching games by metacritic ordering " + ordering);

    }
    public RawgResponseDTO getGamesByTags(String tags, Pageable pageable){
        validateQueryText(tags, "Tags");
        int rawgPageNumber = pageable.getPageNumber() + 1;

        return executeRequest(webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/games")
                       .queryParam("tags", tags)
                                            .queryParam("key", apiKey)
                                            .queryParam("page_size", "20")
                        .queryParam("page", rawgPageNumber)
                        .build()), RawgResponseDTO.class, "Fetching games by tags " + tags);

    }

    private <T> T executeRequest(WebClient.RequestHeadersSpec<?> request, Class<T> responseType, String operation) {
        return request.retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                       response.bodyToMono(String.class)
                               .defaultIfEmpty("")
                               .flatMap(body -> {
                                   String message = operation + " failed with status " + response.statusCode()
                                           + (body.isBlank() ? "" : ": " + body);
                                   logger.error(message);
                                   return Mono.error(new IllegalStateException(message));
                               }))
                .bodyToMono(responseType)
                .switchIfEmpty(Mono.error(new IllegalStateException(operation + " returned an empty response.")))
                .onErrorMap(WebClientRequestException.class,
                       e -> new IllegalStateException(operation + " could not reach RAWG.", e))
                .block();
    }

    private void validateQueryText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }
}
