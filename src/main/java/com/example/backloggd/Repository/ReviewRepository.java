package com.example.backloggd.Repository;

import com.example.backloggd.Models.ReviewModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewModel, Long> {
    List<ReviewModel> findByGameGameName(String gameName);

    ReviewModel findByGameGameNameAndUserModelUserName(String gameName, String userName);
}
