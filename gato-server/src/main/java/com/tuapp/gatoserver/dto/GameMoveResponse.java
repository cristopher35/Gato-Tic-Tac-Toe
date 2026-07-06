package com.tuapp.gatoserver.dto;

import com.tuapp.gatoserver.model.PlayerSymbol;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameMoveResponse {
    private Integer moveNumber;
    private Long playerId;
    private PlayerSymbol symbol;
    private Integer position;
    private LocalDateTime playedAt;
}
