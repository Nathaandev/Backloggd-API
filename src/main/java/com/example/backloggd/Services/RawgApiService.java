package com.example.backloggd.Services;

import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@Service
public class RawgApiService {

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
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/games")
                        .queryParam("search", gameName)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(RawgResponseDTO.class)
                .block();
    }
    public RawgGameDTO GetGameDetailsWithID(Integer rawgId){
        try {
            return webClient.get()
                            .uri(uriBuilder -> uriBuilder.path("/games/{id}")
                                                         .queryParam("key", apiKey)
                                                         .build(rawgId))
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, ClientResponse -> {
                                System.out.println("RAWG API returned a HTTP error. " + ClientResponse.statusCode());
                                return Mono.empty();
                            })
                            .bodyToMono(RawgGameDTO.class)
                            .block();

        } catch (
                WebClientRequestException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    public RawgResponseDTO getGamesByGenre(String genres){
        return webClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/games")
                                                     .queryParam("genres", genres)
                                                     .queryParam("key", apiKey)
                                                     //Set the page size to 20
                                                     .queryParam("page_size", "20")
                                                     .build())
                        .retrieve()
                        .bodyToMono(RawgResponseDTO.class)
                        .block();
    }
}
