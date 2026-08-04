package com.example.backloggd.Services;

import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Exceptions.UsernameAlreadyInUseException;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Repository.GameRepository;
import com.example.backloggd.Repository.ReviewRepository;
import com.example.backloggd.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GameService gameService;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void signUpRejectsDuplicateUsernames() {
        UserRegistrationDTO request = new UserRegistrationDTO("jane", "secret123");
        when(userRepository.findByUserName("jane")).thenReturn(new UserModel("jane", "encoded"));

        assertThrows(UsernameAlreadyInUseException.class, () -> userService.signUp(request));
    }
}
