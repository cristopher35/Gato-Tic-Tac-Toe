package com.tuapp.gatoserver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_x_id", nullable = false)
    private Long playerXId;

    @Column(name = "player_o_id")
    private Long playerOId;

    @Column(name = "board", length = 9, nullable = false)
    private String board;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_turn", nullable = false)
    private PlayerSymbol currentTurn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "winner")
    private GameResult winner;

    @Enumerated(EnumType.STRING)
    @Column(name = "game_type", nullable = false)
    private GameType gameType;

    @Column(name = "turn_timeout_seconds")
    private Integer turnTimeoutSeconds;

    @Column(name = "last_move_at")
    private LocalDateTime lastMoveAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;
}
