package com.example.backloggd.Services;

import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Exceptions.UsernameAlreadyInUseException;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void signUpRejectsDuplicateUsernames() {
        UserRegistrationDTO request = new UserRegistrationDTO("jane", "secret123");
        when(userRepository.findByUserName("jane")).thenReturn(new UserModel("jane", "encoded"));

        assertThrows(UsernameAlreadyInUseException.class, () -> userService.signUp(request));
    }
}
