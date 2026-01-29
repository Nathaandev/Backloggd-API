package com.example.backloggd.Repository;

import java.util.Optional;

import com.example.backloggd.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    UserDetails findByLogin(String login);
}
