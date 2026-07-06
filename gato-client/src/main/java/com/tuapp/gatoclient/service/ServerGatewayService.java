package com.tuapp.gatoclient.service;

import com.tuapp.gatoclient.client.GatoServerClient;
import com.tuapp.gatoclient.dto.ServerHealthResponse;
import com.tuapp.gatoclient.exception.ClientException;
import com.tuapp.gatoclient.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerGatewayService {

    private final GatoServerClient serverClient;
    private final GameRegistryService gameRegistryService;
    private final PlayerRepository playerRepository;

    public ResponseEntity<Object> createGame(String serverUrl, String gameType, Integer turnTimeoutSeconds) {
        Long playerId = getAuthenticatedPlayerId();

        Map<String, Object> body = turnTimeoutSeconds != null
            ? Map.of("playerId", playerId, "gameType", gameType, "turnTimeoutSeconds", turnTimeoutSeconds)
            : Map.of("playerId", playerId, "gameType", gameType);

        ResponseEntity<Object> response = serverClient.post(serverUrl, "/api/games", body);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() instanceof Map<?,?> gameData) {
            Object gameIdObj = gameData.get("id");
            if (gameIdObj instanceof Number) {
                Long gameId = ((Number) gameIdObj).longValue();
                gameRegistryService.register(gameId, serverUrl, playerId);
            }
        }

        return response;
    }

    public ResponseEntity<Object> joinGame(Long gameId, String serverUrl) {
        Long playerId = getAuthenticatedPlayerId();
        Map<String, Object> body = Map.of("playerId", playerId);
        ResponseEntity<Object> response = serverClient.post(serverUrl, "/api/games/" + gameId + "/join", body);

        if (response.getStatusCode().is2xxSuccessful()) {
            gameRegistryService.register(gameId, serverUrl, playerId);
        }

        return response;
    }

    public ResponseEntity<Object> makeMove(Long gameId, int position) {
        Long playerId = getAuthenticatedPlayerId();
        String serverUrl = gameRegistryService.getServerUrl(gameId, playerId);
        Map<String, Object> body = Map.of("playerId", playerId, "position", position);
        return serverClient.post(serverUrl, "/api/games/" + gameId + "/move", body);
    }

    public ResponseEntity<Object> getGameState(Long gameId) {
        Long playerId = getAuthenticatedPlayerId();
        String serverUrl = gameRegistryService.getServerUrl(gameId, playerId);
        return serverClient.get(serverUrl, "/api/games/" + gameId);
    }

    public Object getMyGames() {
        Long playerId = getAuthenticatedPlayerId();
        return gameRegistryService.getGamesByPlayerGroupedByServer(playerId);
    }

    public ServerHealthResponse checkServerHealth(String serverUrl) {
        long latency = serverClient.pingAndMeasure(serverUrl);
        return ServerHealthResponse.builder()
            .serverUrl(serverUrl)
            .available(latency >= 0)
            .latencyMs(latency >= 0 ? latency : null)
            .checkedAt(java.time.LocalDateTime.now())
            .build();
    }

    public ResponseEntity<Object> getGameHistory(Long gameId) {
        Long playerId = getAuthenticatedPlayerId();
        String serverUrl = gameRegistryService.getServerUrl(gameId, playerId);
        return serverClient.get(serverUrl, "/api/games/" + gameId + "/history");
    }

    private Long getAuthenticatedPlayerId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return playerRepository.findByUsername(username)
            .orElseThrow(() -> new ClientException("Jugador no autenticado", 401))
            .getId();
    }
}
