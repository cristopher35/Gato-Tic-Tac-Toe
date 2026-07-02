CREATE TABLE games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_x_id BIGINT NOT NULL,
    player_o_id BIGINT NULL,
    board VARCHAR(9) NOT NULL,
    current_turn VARCHAR(1) NOT NULL,
    status VARCHAR(20) NOT NULL,
    winner VARCHAR(10) NULL,
    game_type VARCHAR(10) NOT NULL,
    turn_timeout_seconds INT NULL,
    last_move_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    started_at DATETIME NULL
);

CREATE INDEX idx_games_status ON games(status);
CREATE INDEX idx_games_player_x ON games(player_x_id);
CREATE INDEX idx_games_player_o ON games(player_o_id);
