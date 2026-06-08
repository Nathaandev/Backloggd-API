package com.example.backloggd.Services;

import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<UserModel> signUp(UserRegistrationDTO userRegistrationDTO){
        UserModel userModel = new UserModel(userRegistrationDTO.userName(), userRegistrationDTO.password(), userRegistrationDTO.userEmail());
        return ResponseEntity.ok(userRepository.save(userModel));
    }

}
