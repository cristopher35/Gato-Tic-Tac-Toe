package com.tuapp.gatoserver.dto;

import com.tuapp.gatoserver.model.GameResult;
import com.tuapp.gatoserver.model.GameStatus;
import com.tuapp.gatoserver.model.GameType;
import com.tuapp.gatoserver.model.PlayerSymbol;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameResponse {

    private Long id;
    private Long playerXId;
    private Long playerOId;
    private String board;
    private PlayerSymbol currentTurn;
    private GameStatus status;
    private GameResult winner;
    private GameType gameType;
    private Integer turnTimeoutSeconds;
    private Long remainingSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime lastMoveAt;
}
