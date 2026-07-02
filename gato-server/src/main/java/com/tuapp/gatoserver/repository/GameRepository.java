package com.tuapp.gatoserver.repository;

import com.tuapp.gatoserver.model.Game;
import com.tuapp.gatoserver.model.GameStatus;
import com.tuapp.gatoserver.model.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    List<Game> findByStatus(GameStatus status);

    List<Game> findByGameType(GameType gameType);

    List<Game> findByStatusAndGameType(GameStatus status, GameType gameType);

    @Query("SELECT g FROM Game g WHERE g.playerXId = :playerId OR g.playerOId = :playerId")
    List<Game> findAllByPlayerId(@Param("playerId") Long playerId);

    Optional<Game> findById(Long id);
}
