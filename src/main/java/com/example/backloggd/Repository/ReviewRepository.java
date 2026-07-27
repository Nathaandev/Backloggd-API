package com.example.backloggd.Repository;

import com.example.backloggd.Models.ReviewModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewModel, Long> {
    List<ReviewModel> findByGameGameName(String gameName);

    Optional<ReviewModel> findByGameGameNameAndUserModelUserName(String gameName, String userName);
}
