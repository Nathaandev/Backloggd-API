package com.example.backloggd.Controller;

import com.example.backloggd.DTO.UserProfileDTO;
import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Services.UserService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
    }

    @Test
    void signupEndpointReturnsRegisteredUser() throws Exception {
        UserModel user = new UserModel("jane", "encoded");
        when(userService.signUp(any(UserRegistrationDTO.class))).thenReturn(ResponseEntity.ok(user));

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserRegistrationDTO("jane", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("jane"));
    }

    @Test
    void profileEndpointReturnsAuthenticatedUserProfile() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("jane", null));
        UserProfileDTO profile = new UserProfileDTO("jane", List.of());
        when(userService.getProfile(any())).thenReturn(ResponseEntity.ok(profile));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("jane"));
    }
}
