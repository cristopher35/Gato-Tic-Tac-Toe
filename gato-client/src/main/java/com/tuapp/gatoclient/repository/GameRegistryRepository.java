package com.tuapp.gatoclient.repository;

import com.tuapp.gatoclient.model.GameRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRegistryRepository extends JpaRepository<GameRegistry, Long> {
    Optional<GameRegistry> findByGameIdAndPlayerId(Long gameId, Long playerId);
    List<GameRegistry> findAllByPlayerId(Long playerId);
    Optional<GameRegistry> findByGameId(Long gameId);
}
