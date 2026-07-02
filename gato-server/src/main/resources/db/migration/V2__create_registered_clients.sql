CREATE TABLE registered_clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL UNIQUE,
    client_secret VARCHAR(255) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    registered_at DATETIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
