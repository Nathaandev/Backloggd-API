package com.example.backloggd.Services;

import com.example.backloggd.DTO.UserRegistrationDTO;
import com.example.backloggd.Models.UserModel;
import com.example.backloggd.Repository.UserRepository;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public ResponseEntity<UserModel> signUp(UserRegistrationDTO userRegistrationDTO){
        UserModel userModel = new UserModel(userRegistrationDTO.userName(), passwordEncoder.encode(userRegistrationDTO.password()), userRegistrationDTO.userEmail());
        return ResponseEntity.ok(userRepository.save(userModel));
    }
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserModel user = userRepository.findByUserName(userName);
    return User.withUsername(user.getUserName())
            .password(user.getPassword())
            .roles("USER")
            .build();
    }

}