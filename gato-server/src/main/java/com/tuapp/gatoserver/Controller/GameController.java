package com.tuapp.gatoserver.controller;

import com.tuapp.gatoserver.dto.CreateGameRequest;
import com.tuapp.gatoserver.dto.GameMoveResponse;
import com.tuapp.gatoserver.dto.GameResponse;
import com.tuapp.gatoserver.dto.JoinGameRequest;
import com.tuapp.gatoserver.dto.MoveRequest;
import com.tuapp.gatoserver.model.GameStatus;
import com.tuapp.gatoserver.model.GameType;
import com.tuapp.gatoserver.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Games", description = "Gestión de partidas del juego Gato")
public class GameController {

    private final GameService gameService;

    @Operation(summary = "Crear una nueva partida")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Partida creada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<GameResponse> createGame(@Valid @RequestBody CreateGameRequest request) {
        log.info("POST /api/games - playerId={} type={}", request.getPlayerId(), request.getGameType());
        GameResponse response = gameService.createGame(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar partidas con filtros opcionales")
    @ApiResponse(responseCode = "200", description = "Lista de partidas")
    @GetMapping
    public ResponseEntity<List<GameResponse>> listGames(
            @RequestParam(required = false) GameStatus status,
            @RequestParam(required = false) GameType gameType) {
        return ResponseEntity.ok(gameService.listGames(status, gameType));
    }

    @Operation(summary = "Estado completo de una partida")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Estado de la partida"),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada")
    })
    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable Long gameId) {
        log.info("GET /api/games/{}", gameId);
        return ResponseEntity.ok(gameService.getGame(gameId));
    }

    @Operation(summary = "Unirse a una partida como jugador O")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Unido a la partida"),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada"),
        @ApiResponse(responseCode = "409", description = "Partida ya no está disponible")
    })
    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameResponse> joinGame(
            @PathVariable Long gameId,
            @Valid @RequestBody JoinGameRequest request) {
        log.info("POST /api/games/{}/join - playerId={}", gameId, request.getPlayerId());
        return ResponseEntity.ok(gameService.joinGame(gameId, request));
    }

    @Operation(summary = "Realizar un movimiento")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Movimiento registrado"),
        @ApiResponse(responseCode = "400", description = "Movimiento inválido"),
        @ApiResponse(responseCode = "403", description = "No eres participante"),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada"),
        @ApiResponse(responseCode = "422", description = "Partida no está en progreso")
    })
    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameResponse> makeMove(
            @PathVariable Long gameId,
            @Valid @RequestBody MoveRequest request) {
        log.info("POST /api/games/{}/move - playerId={} pos={}", gameId, request.getPlayerId(), request.getPosition());
        return ResponseEntity.ok(gameService.makeMove(gameId, request));
    }

    @Operation(summary = "Partidas de un jugador en este server")
    @ApiResponse(responseCode = "200", description = "Lista de partidas del jugador")
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<GameResponse>> getGamesByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(gameService.getGamesByPlayer(playerId));
    }

    @Operation(summary = "Historial de movimientos de una partida")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historial de movimientos"),
        @ApiResponse(responseCode = "404", description = "Partida no encontrada")
    })
    @GetMapping("/{gameId}/history")
    public ResponseEntity<List<GameMoveResponse>> getHistory(@PathVariable Long gameId) {
        log.info("GET /api/games/{}/history", gameId);
        return ResponseEntity.ok(gameService.getMoveHistory(gameId));
    }
}
