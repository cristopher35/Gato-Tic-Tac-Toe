package com.tuapp.gatoserver.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_moves")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GameMove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "move_number", nullable = false)
    private Integer moveNumber;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "symbol", nullable = false)
    private PlayerSymbol symbol;

    @Column(name = "position", nullable = false)
    private Integer position;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;
}
