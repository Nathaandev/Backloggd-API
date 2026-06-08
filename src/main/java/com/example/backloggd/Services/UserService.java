package com.example.backloggd.Services;

import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Repository.UserRepository;
import com.example.backloggd.security.WebSecurityConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(WebSecurityConfig webSecurityConfig, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public ResponseEntity<UserModel> signUp(UserRegistrationDTO userRegistrationDTO){
        UserModel userModel = new UserModel(userRegistrationDTO.userName(), passwordEncoder.encode(userRegistrationDTO.password()), userRegistrationDTO.userEmail());
        return ResponseEntity.ok(userRepository.save(userModel));
    }

}
