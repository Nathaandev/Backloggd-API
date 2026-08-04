package com.example.backloggd.Controller;

import com.example.backloggd.DTO.GameReviewDTO;
import com.example.backloggd.DTO.ReviewResponseDTO;
import com.example.backloggd.Services.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ReviewService reviewService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ReviewController(reviewService)).build();
    }

    @Test
    void publishReviewEndpointReturnsCreatedReviewResponse() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("jane", null));
        ReviewResponseDTO review = new ReviewResponseDTO("Hades", "jane", "Great game", 4.5f, 40);
        when(reviewService.publishReviews(any(), any(), any(GameReviewDTO.class))).thenReturn(ResponseEntity.ok(review));

        mockMvc.perform(post("/reviews/Hades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GameReviewDTO(4.5f, "Great game", 40))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameName").value("Hades"));
    }

    @Test
    void deleteReviewEndpointReturnsSuccessMessage() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("jane", null));
        when(reviewService.deleteReview(any(), any())).thenReturn(ResponseEntity.ok("Review deleted successfully."));

        mockMvc.perform(delete("/reviews/Hades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$" ).value("Review deleted successfully."));
    }
}
