CREATE TABLE game_moves (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id BIGINT NOT NULL,
    move_number INT NOT NULL,
    player_id BIGINT NOT NULL,
    symbol VARCHAR(1) NOT NULL,
    position INT NOT NULL,
    played_at DATETIME NOT NULL,
    CONSTRAINT fk_game_moves_game FOREIGN KEY (game_id) REFERENCES games(id)
);

CREATE INDEX idx_game_moves_game_id ON game_moves(game_id);
