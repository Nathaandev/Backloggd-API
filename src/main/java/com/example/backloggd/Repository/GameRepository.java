package com.example.backloggd.Repository;

import java.util.Optional;

import com.example.backloggd.Models.GamesModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<GamesModel, Long> {

  Optional<GamesModel> findBygameNameIgnoreCase(String gameName);

  Page<GamesModel> findByMetacriticIsNotNull(Pageable pageable);

  @Query("select count(g) from GamesModel g where g.metacritic is not null")
  long countByMetacriticIsNotNull();
}
