package com.tuapp.gatoclient.controller;

import com.tuapp.gatoclient.dto.CreateGameProxyRequest;
import com.tuapp.gatoclient.dto.MoveProxyRequest;
import com.tuapp.gatoclient.service.ServerGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Game", description = "Proxy de acciones de juego hacia el server")
public class GameProxyController {

    private final ServerGatewayService gatewayService;

    @Operation(summary = "Crear partida en el server indicado")
    @PostMapping("/create")
    public ResponseEntity<Object> createGame(@Valid @RequestBody CreateGameProxyRequest request) {
        log.info("POST /api/game/create - serverUrl={} type={}", request.getServerUrl(), request.getGameType());
        return gatewayService.createGame(request.getServerUrl(), request.getGameType(), request.getTurnTimeoutSeconds());
    }

    @Operation(summary = "Unirse a una partida")
    @PostMapping("/{gameId}/join")
    public ResponseEntity<Object> joinGame(
            @PathVariable Long gameId,
            @RequestParam String serverUrl) {
        log.info("POST /api/game/{}/join - serverUrl={}", gameId, serverUrl);
        return gatewayService.joinGame(gameId, serverUrl);
    }

    @Operation(summary = "Realizar movimiento")
    @PostMapping("/{gameId}/move")
    public ResponseEntity<Object> makeMove(
            @PathVariable Long gameId,
            @Valid @RequestBody MoveProxyRequest request) {
        log.info("POST /api/game/{}/move - position={}", gameId, request.getPosition());
        return gatewayService.makeMove(gameId, request.getPosition());
    }

    @Operation(summary = "Estado actual de la partida")
    @GetMapping("/{gameId}/state")
    public ResponseEntity<Object> getState(@PathVariable Long gameId) {
        return gatewayService.getGameState(gameId);
    }

    @Operation(summary = "Mis partidas agrupadas por server")
    @GetMapping("/my-games")
    public ResponseEntity<Object> myGames() {
        return ResponseEntity.ok(gatewayService.getMyGames());
    }
}
