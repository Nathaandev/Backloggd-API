package com.example.backloggd.Services;

import com.example.backloggd.DTO.GameSummaryDTO;
import com.example.backloggd.DTO.ObjectsDTO.DevelopersDTO;
import com.example.backloggd.DTO.ObjectsDTO.GenreDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsDTO;
import com.example.backloggd.DTO.ObjectsDTO.PlatformsWrapperDTO;
import com.example.backloggd.DTO.ObjectsDTO.PublishersDTO;
import com.example.backloggd.DTO.ObjectsDTO.TagsDTO;
import com.example.backloggd.DTO.RawgGameDTO;
import com.example.backloggd.DTO.RawgResponseDTO;
import com.example.backloggd.Exceptions.RawgApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class RawgApiService {

    private static final String DEFAULT_IGDB_BASE_URL = "https://api.igdb.com/v4";
    private static final String DEFAULT_IGDB_AUTH_URL = "https://id.twitch.tv/oauth2";
    private static final String IGDB_FIELDS = "fields id,name,summary,first_release_date,aggregated_rating,cover.url,genres.id,genres.name,platforms.id,platforms.name,involved_companies.company.id,involved_companies.company.name,involved_companies.developer,involved_companies.publisher,keywords.id,keywords.name; ";

    private final Logger logger = LoggerFactory.getLogger(RawgApiService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient igdbClient;
    private final WebClient authClient;
    private final String clientId;
    private final String clientSecret;

    private volatile String accessToken;
    private volatile Instant accessTokenExpiresAt = Instant.EPOCH;

    public RawgApiService(
            @Value("${igdb.api.base-url:}") String baseUrl,
            @Value("${igdb.auth-url:}") String authUrl,
            @Value("${igdb.client-id:}") String clientId,
            @Value("${igdb.client-secret:}") String clientSecret
    ) {
        this.igdbClient = WebClient.builder().baseUrl(resolveBaseUrl(baseUrl, DEFAULT_IGDB_BASE_URL)).build();
        this.authClient = WebClient.builder().baseUrl(resolveBaseUrl(authUrl, DEFAULT_IGDB_AUTH_URL)).build();
        this.clientId = normalizeCredential(clientId);
        this.clientSecret = normalizeCredential(clientSecret);
    }

    public RawgResponseDTO getGames(String gameName){
        validateQueryText(gameName, "Game name");
        return fetchGames(buildSearchQuery(gameName));
    }

    public RawgGameDTO GetGameDetailsWithID(Integer rawgId){
        if (rawgId == null) {
            throw new IllegalArgumentException("IGDB game id is required.");
        }
        RawgResponseDTO response = fetchGames(buildIdQuery(rawgId));
        if (response == null || response.results() == null || response.results().isEmpty()) {
            return null;
        }
        return response.results().get(0);
    }

    public RawgResponseDTO getGamesByGenre(String genres, Pageable pageable){
        validateQueryText(genres, "Genre");
        return fetchWithFallback(genres, pageable, "genre", this::buildGenreQuery);
    }

    public RawgResponseDTO getGamesByDeveloper(String developer, Pageable pageable){
        validateQueryText(developer, "Developer");
        return fetchWithFallback(developer, pageable, "developer", this::buildDeveloperQuery);
    }

    public RawgResponseDTO getGamesByPublishers(String publisher, Pageable pageable){
        validateQueryText(publisher, "Publisher");
        return fetchWithFallback(publisher, pageable, "publisher", this::buildPublisherQuery);
    }

    public RawgResponseDTO getGamesByMetacritic(String ordering, Pageable pageable){
        validateQueryText(ordering, "Ordering");
        return fetchWithFallback(ordering, pageable, "metacritic", this::buildMetacriticQuery);
    }

    public RawgResponseDTO getGamesByTags(String tags, Pageable pageable){
        validateQueryText(tags, "Tags");
        return fetchWithFallback(tags, pageable, "tags", this::buildTagsQuery);
    }

    private RawgResponseDTO fetchWithFallback(String value, Pageable pageable, String operationName, QueryBuilder queryBuilder) {
        String normalized = value.trim();
        RawgResponseDTO response = null;
        for (String searchTerm : buildSearchTerms(normalized)) {
            response = fetchGames(queryBuilder.build(searchTerm, pageable));
            if (response != null && response.results() != null && !response.results().isEmpty()) {
                return response;
            }
        }
        logger.info("IGDB {} search for '{}' returned no results.", operationName, value);
        return response;
    }

    private RawgResponseDTO fetchGames(String query) {
        ResponseEntity<String> entity = igdbClient.post()
                .uri("/games")
                .header("Client-ID", clientId)
                .header("Authorization", "Bearer " + ensureAccessToken())
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue(query)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> {
                                    String message = "IGDB request failed with status " + response.statusCode()
                                            + (body.isBlank() ? "" : ": " + body);
                                    logger.error(message);
                                    return Mono.error(new RawgApiException(message));
                                }))
                .toEntity(String.class)
                .onErrorMap(WebClientRequestException.class,
                        e -> new RawgApiException("IGDB request could not reach the API.", e))
                .block();

        if (entity == null || entity.getBody() == null || entity.getBody().isBlank()) {
            return new RawgResponseDTO(List.of(), 0);
        }

        return parseResponse(entity.getBody(), entity.getHeaders());
    }

    private RawgResponseDTO parseResponse(String body, HttpHeaders headers) {
        try {
            JsonNode root = objectMapper.readTree(body);
            if (!root.isArray()) {
                return new RawgResponseDTO(List.of(), parseCount(headers, 0));
            }

            List<RawgGameDTO> results = new ArrayList<>();
            for (JsonNode gameNode : root) {
                RawgGameDTO game = toGameDTO(gameNode);
                if (game != null) {
                    results.add(game);
                }
            }
            return new RawgResponseDTO(results, parseCount(headers, results.size()));
        } catch (Exception e) {
            throw new RawgApiException("Failed to parse IGDB response.", e);
        }
    }

    private RawgGameDTO toGameDTO(JsonNode gameNode) {
        Integer id = readInteger(gameNode, "id");
        String name = readText(gameNode, "name");
        if (id == null || name == null || name.isBlank()) {
            return null;
        }

        String releaseDate = formatReleaseDate(gameNode.path("first_release_date"));
        Integer metacritic = readDoubleAsInteger(gameNode.path("aggregated_rating"));

        List<GenreDTO> genres = readGenres(gameNode.path("genres"));
        List<PlatformsWrapperDTO> platforms = readPlatforms(gameNode.path("platforms"));
        List<TagsDTO> tags = readTags(gameNode.path("keywords"));
        List<DevelopersDTO> developers = readCompanies(gameNode.path("involved_companies"), true);
        List<PublishersDTO> publishers = readPublishers(gameNode.path("involved_companies"));
        String coverUrl = formatCoverUrl(gameNode.path("cover").path("url"));

        return new RawgGameDTO(
                id,
                name,
                readText(gameNode, "summary"),
                releaseDate,
                publishers,
                metacritic,
                developers,
                genres,
                platforms,
                tags,
                coverUrl
        );
    }

    private List<GenreDTO> readGenres(JsonNode node) {
        List<GenreDTO> genres = new ArrayList<>();
        if (!node.isArray()) {
            return genres;
        }
        for (JsonNode item : node) {
            Integer id = readInteger(item, "id");
            String name = readText(item, "name");
            if (id != null && name != null && !name.isBlank()) {
                genres.add(new GenreDTO(id, name));
            }
        }
        return genres;
    }

    private List<PlatformsWrapperDTO> readPlatforms(JsonNode node) {
        List<PlatformsWrapperDTO> platforms = new ArrayList<>();
        if (!node.isArray()) {
            return platforms;
        }
        for (JsonNode item : node) {
            Integer id = readInteger(item, "id");
            String name = readText(item, "name");
            if (id != null && name != null && !name.isBlank()) {
                platforms.add(new PlatformsWrapperDTO(new PlatformsDTO(id, name)));
            }
        }
        return platforms;
    }

    private List<TagsDTO> readTags(JsonNode node) {
        List<TagsDTO> tags = new ArrayList<>();
        if (!node.isArray()) {
            return tags;
        }
        for (JsonNode item : node) {
            Integer id = readInteger(item, "id");
            String name = readText(item, "name");
            if (id != null && name != null && !name.isBlank()) {
                tags.add(new TagsDTO(id, name));
            }
        }
        return tags;
    }

    private List<DevelopersDTO> readCompanies(JsonNode node, boolean developersOnly) {
        List<DevelopersDTO> developers = new ArrayList<>();
        if (!node.isArray()) {
            return developers;
        }
        Set<Integer> seen = new LinkedHashSet<>();
        for (JsonNode item : node) {
            if (!item.path("developer").asBoolean(false)) {
                continue;
            }
            JsonNode company = item.path("company");
            Integer id = readInteger(company, "id");
            String name = readText(company, "name");
            if (id != null && name != null && !name.isBlank() && seen.add(id)) {
                developers.add(new DevelopersDTO(id, name));
            }
        }
        return developers;
    }

    private List<PublishersDTO> readPublishers(JsonNode node) {
        List<PublishersDTO> publishers = new ArrayList<>();
        if (!node.isArray()) {
            return publishers;
        }
        Set<Integer> seen = new LinkedHashSet<>();
        for (JsonNode item : node) {
            if (!item.path("publisher").asBoolean(false)) {
                continue;
            }
            JsonNode company = item.path("company");
            Integer id = readInteger(company, "id");
            String name = readText(company, "name");
            if (id != null && name != null && !name.isBlank() && seen.add(id)) {
                publishers.add(new PublishersDTO(id, name));
            }
        }
        return publishers;
    }

    private String ensureAccessToken() {
        if (accessToken != null && Instant.now().isBefore(accessTokenExpiresAt)) {
            return accessToken;
        }
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException("IGDB credentials are required before calling the API.");
        }

        String body = authClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/token")
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("grant_type", "client_credentials")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (body == null || body.isBlank()) {
            throw new RawgApiException("IGDB auth returned an empty response.");
        }

        try {
            JsonNode json = objectMapper.readTree(body);
            String token = readText(json, "access_token");
            int expiresIn = json.path("expires_in").asInt(0);
            if (token == null || token.isBlank()) {
                throw new RawgApiException("IGDB auth did not return an access token.");
            }
            accessToken = token;
            accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(60, expiresIn - 60L));
            return accessToken;
        } catch (Exception e) {
            throw new RawgApiException("Failed to parse IGDB auth response.", e);
        }
    }

    private int parseCount(HttpHeaders headers, int fallback) {
        String header = headers.getFirst("X-Count");
        if (header == null || header.isBlank()) {
            header = headers.getFirst("x-count");
        }
        if (header == null || header.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(header);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String buildSearchQuery(String value) {
        return IGDB_FIELDS + "search \"" + escapeQueryValue(value) + "\"; limit 20; offset 0; count;";
    }

    private String buildIdQuery(Integer rawgId) {
        return IGDB_FIELDS + "where id = " + rawgId + "; limit 1; offset 0; count;";
    }

    private String buildGenreQuery(String value, Pageable pageable) {
        return IGDB_FIELDS
                + "where genres.name ~ *\"" + escapeQueryValue(value) + "\"*; "
                + pagingClause(pageable)
                + "count;";
    }

    private String buildDeveloperQuery(String value, Pageable pageable) {
        return IGDB_FIELDS
                + "where involved_companies.developer = true & involved_companies.company.name ~ *\"" + escapeQueryValue(value) + "\"*; "
                + pagingClause(pageable)
                + "count;";
    }

    private String buildPublisherQuery(String value, Pageable pageable) {
        return IGDB_FIELDS
                + "where involved_companies.publisher = true & involved_companies.company.name ~ *\"" + escapeQueryValue(value) + "\"*; "
                + pagingClause(pageable)
                + "count;";
    }

    private String buildMetacriticQuery(String value, Pageable pageable) {
        String direction = normalizeOrdering(value);
        return IGDB_FIELDS
                + "sort aggregated_rating " + direction + "; "
                + pagingClause(pageable)
                + "count;";
    }

    private String buildTagsQuery(String value, Pageable pageable) {
        return IGDB_FIELDS
                + "where keywords.name ~ *\"" + escapeQueryValue(value) + "\"*; "
                + pagingClause(pageable)
                + "count;";
    }

    private String pagingClause(Pageable pageable) {
        int limit = pageable == null ? 20 : Math.max(1, pageable.getPageSize());
        int offset = pageable == null ? 0 : Math.max(0, pageable.getPageNumber()) * limit;
        return "limit " + limit + "; offset " + offset + "; ";
    }

    private String normalizeOrdering(String ordering) {
        String normalized = ordering.trim().toLowerCase(Locale.ROOT);
        if ("asc".equals(normalized) || "desc".equals(normalized)) {
            return normalized;
        }
        if (normalized.contains("aggregated_rating")) {
            return normalized.contains("asc") ? "asc" : "desc";
        }
        return "desc";
    }

    private List<String> buildSearchTerms(String value) {
        String normalized = value.trim();
        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        String titleCase = toTitleCase(normalized);
        String slug = toSlug(normalized);

        List<String> searchTerms = new ArrayList<>();
        addSearchTerm(searchTerms, normalized);
        addSearchTerm(searchTerms, lowerCase);
        addSearchTerm(searchTerms, titleCase);
        addSearchTerm(searchTerms, slug);
        return searchTerms;
    }

    private void validateQueryText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }

    private void addSearchTerm(List<String> searchTerms, String searchTerm) {
        if (searchTerm != null && !searchTerm.isBlank() && !searchTerms.contains(searchTerm)) {
            searchTerms.add(searchTerm);
        }
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

    private String toSlug(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9]+", "-");
        return normalized.replaceAll("^-|-$", "");
    }

    private String escapeQueryValue(String value) {
        return value.replace("\"", "\\\"");
    }

    private String normalizeCredential(String value) {
        return value == null ? "" : value.trim();
    }

    private String resolveBaseUrl(String configured, String fallback) {
        String value = configured == null ? "" : configured.trim();
        return value.isEmpty() ? fallback : value;
    }

    private Integer readInteger(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isMissingNode() || value.isNull() ? null : value.asInt();
    }

    private String readText(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private Integer readDoubleAsInteger(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        double value = node.asDouble();
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        }
        return (int) Math.round(value);
    }

    private String formatReleaseDate(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        long epochSeconds = node.asLong();
        return LocalDate.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC).toString();
    }

    private String formatCoverUrl(JsonNode node) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.startsWith("//")) {
            value = "https:" + value;
        }
        return value.replace("t_thumb", "t_cover_big");
    }

    @FunctionalInterface
    private interface QueryBuilder {
        String build(String value, Pageable pageable);
    }
}
