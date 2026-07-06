package com.tuapp.gatoserver.repository;

import com.tuapp.gatoserver.model.GameMove;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameMoveRepository extends JpaRepository<GameMove, Long> {
    List<GameMove> findByGameIdOrderByMoveNumberAsc(Long gameId);
    int countByGameId(Long gameId);
}
