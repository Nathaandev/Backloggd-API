package com.example.backloggd.Services;

import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import com.example.backloggd.Exceptions.RawgApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        RawgResponseDTO response = null;
        for (String searchTerm : buildSearchTerms(gameName)) {
            response = executeRequest(webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/games")
                           .queryParam("search", searchTerm)
                           .queryParam("key", apiKey)
                           .build()), RawgResponseDTO.class, "Fetching games for " + searchTerm);
            if (response != null && response.results() != null && !response.results().isEmpty()) {
                return response;
            }
        }
        return response;
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
        int rawgPageNumber = resolveRawgPageNumber(pageable);

        RawgResponseDTO response = null;
        for (String searchTerm : buildSearchTerms(genres)) {
            response = executeRequest(webClient.get()
                           .uri(uriBuilder -> uriBuilder.path("/games")
                                                            .queryParam("genres", searchTerm)
                                                            .queryParam("key", apiKey)
                                                            //Set the page size to 20
                                                            .queryParam("page_size", "20")
                                                            .queryParam("page", rawgPageNumber)
                                                            .build()), RawgResponseDTO.class, "Fetching games by genre " + searchTerm);
            if (response != null && response.results() != null && !response.results().isEmpty()) {
                return response;
            }
        }
        return response;
    }
    public RawgResponseDTO getGamesByDeveloper(String developer, Pageable pageable){
        validateQueryText(developer, "Developer");
        int rawgPageNumber = resolveRawgPageNumber(pageable);

        RawgResponseDTO response = null;
        for (String searchTerm : buildSearchTerms(developer)) {
            response = executeRequest(webClient.get()
                           .uri(uriBuilder -> uriBuilder.path("/games")
                                                            .queryParam("developers", searchTerm)
                                                            .queryParam("key", apiKey)
                                                            //Set the page size to 20
                                                            .queryParam("page_size", "20")
                                                            .queryParam("page", rawgPageNumber)
                                                            .build()), RawgResponseDTO.class, "Fetching games by developer " + searchTerm);
            if (response != null && response.results() != null && !response.results().isEmpty()) {
                return response;
            }
        }
        return response;
    }
    public RawgResponseDTO getGamesByPublishers(String publisher, Pageable pageable){
        validateQueryText(publisher, "Publisher");
        int rawgPageNumber = resolveRawgPageNumber(pageable);

        RawgResponseDTO response = null;
        for (String searchTerm : buildSearchTerms(publisher)) {
            response = executeRequest(webClient.get()
                           .uri(uriBuilder -> uriBuilder.path("/games")
                                                            .queryParam("publishers", searchTerm)
                                                            .queryParam("key", apiKey)
                                                            //Set the page size to 20
                                                            .queryParam("page_size", "20")
                                                            .queryParam("page", rawgPageNumber)
                                                            .build()), RawgResponseDTO.class, "Fetching games by publisher " + searchTerm);
            if (response != null && response.results() != null && !response.results().isEmpty()) {
                return response;
            }
        }
        return response;

    }
    public RawgResponseDTO getGamesByMetacritic(String ordering, Pageable pageable){
        validateQueryText(ordering, "Ordering");
        int rawgPageNumber = resolveRawgPageNumber(pageable);

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
        int rawgPageNumber = resolveRawgPageNumber(pageable);

        RawgResponseDTO response = null;
        for (String searchTerm : buildSearchTerms(tags)) {
            response = executeRequest(webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/games")
                           .queryParam("tags", searchTerm)
                                                .queryParam("key", apiKey)
                                                .queryParam("page_size", "20")
                           .queryParam("page", rawgPageNumber)
                           .build()), RawgResponseDTO.class, "Fetching games by tags " + searchTerm);
            if (response != null && response.results() != null && !response.results().isEmpty()) {
                return response;
            }
        }
        return response;

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
                                   return Mono.error(new RawgApiException(message));
                               }))
                .bodyToMono(responseType)
                .switchIfEmpty(Mono.error(new RawgApiException(operation + " returned an empty response.")))
                .onErrorMap(WebClientRequestException.class,
                       e -> new RawgApiException(operation + " could not reach RAWG.", e))
                .block();
    }

    private void validateQueryText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private int resolveRawgPageNumber(Pageable pageable) {
        return Math.max(1, pageable.getPageNumber());
    }

    private List<String> buildSearchTerms(String gameName) {
        String normalized = gameName.trim();
        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        String titleCase = toTitleCase(normalized);

        List<String> searchTerms = new ArrayList<>();
        searchTerms.add(normalized);
        if (!lowerCase.equals(normalized)) {
            searchTerms.add(lowerCase);
        }
        if (!titleCase.equals(normalized) && !titleCase.equals(lowerCase)) {
            searchTerms.add(titleCase);
        }
        return searchTerms;
    }

    private String toTitleCase(String value) {
        String[] parts = value.toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }
}
