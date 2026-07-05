package com.tuapp.gatoclient.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_registry")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "server_url", nullable = false)
    private String serverUrl;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
}
