CREATE TABLE game_registry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    server_url VARCHAR(255) NOT NULL,
    player_id BIGINT NOT NULL,
    registered_at DATETIME NOT NULL,
    FOREIGN KEY (player_id) REFERENCES players(id)
);
