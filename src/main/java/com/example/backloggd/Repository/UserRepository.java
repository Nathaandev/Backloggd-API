package com.example.backloggd.Repository;

import java.util.Optional;

import com.example.backloggd.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByUsername(String username);
}
