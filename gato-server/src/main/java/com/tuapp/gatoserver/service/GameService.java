package com.tuapp.gatoserver.service;

import com.tuapp.gatoserver.dto.CreateGameRequest;
import com.tuapp.gatoserver.dto.GameMoveResponse;
import com.tuapp.gatoserver.dto.GameResponse;
import com.tuapp.gatoserver.dto.JoinGameRequest;
import com.tuapp.gatoserver.dto.MoveRequest;
import com.tuapp.gatoserver.exception.GameNotFoundException;
import com.tuapp.gatoserver.exception.InvalidMoveException;
import com.tuapp.gatoserver.model.Game;
import com.tuapp.gatoserver.model.GameMove;
import com.tuapp.gatoserver.model.GameResult;
import com.tuapp.gatoserver.model.GameStatus;
import com.tuapp.gatoserver.model.GameType;
import com.tuapp.gatoserver.model.PlayerSymbol;
import com.tuapp.gatoserver.repository.GameMoveRepository;
import com.tuapp.gatoserver.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameRepository gameRepository;
    private final GameValidatorService validator;
    private final GameMoveRepository gameMoveRepository;

    // ─────────────────────────────────────────────
    // Crear partida
    // ─────────────────────────────────────────────

    @Transactional
    public GameResponse createGame(CreateGameRequest request) {
        if (request.getGameType() == GameType.TIMED && request.getTurnTimeoutSeconds() == null) {
            throw new InvalidMoveException("Las partidas TIMED requieren turnTimeoutSeconds (10-300)", 400);
        }
        if (request.getGameType() == GameType.NO_TIME && request.getTurnTimeoutSeconds() != null) {
            throw new InvalidMoveException("Las partidas NO_TIME no deben incluir turnTimeoutSeconds", 400);
        }

        Game game = Game.builder()
                .playerXId(request.getPlayerId())
                .board(".........")
                .currentTurn(PlayerSymbol.X)
                .status(GameStatus.WAITING)
                .gameType(request.getGameType())
                .turnTimeoutSeconds(request.getTurnTimeoutSeconds())
                .createdAt(LocalDateTime.now())
                .build();

        Game saved = gameRepository.save(game);
        log.info("Partida creada: gameId={} playerXId={} type={}", saved.getId(), saved.getPlayerXId(), saved.getGameType());
        return toResponse(saved);
    }

    // ─────────────────────────────────────────────
    // Listar partidas
    // ─────────────────────────────────────────────

    public List<GameResponse> listGames(GameStatus status, GameType gameType) {
        List<Game> games;
        if (status != null && gameType != null) {
            games = gameRepository.findByStatusAndGameType(status, gameType);
        } else if (status != null) {
            games = gameRepository.findByStatus(status);
        } else if (gameType != null) {
            games = gameRepository.findByGameType(gameType);
        } else {
            games = gameRepository.findAll();
        }
        return games.stream().map(this::toResponse).toList();
    }

    // ─────────────────────────────────────────────
    // Estado de una partida
    // ─────────────────────────────────────────────

    @Transactional
    public GameResponse getGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        // Detección lazy de timeout
        if (game.getStatus() == GameStatus.IN_PROGRESS && validator.isTurnExpired(game)) {
            game = applyTimeout(game);
        }

        return toResponse(game);
    }

    // ─────────────────────────────────────────────
    // Unirse a una partida
    // ─────────────────────────────────────────────

    @Transactional
    public GameResponse joinGame(Long gameId, JoinGameRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        if (game.getStatus() != GameStatus.WAITING) {
            throw new InvalidMoveException("La partida no está disponible para unirse.", 409);
        }
        if (game.getPlayerXId().equals(request.getPlayerId())) {
            throw new InvalidMoveException("No puedes unirte a tu propia partida.", 409);
        }

        game.setPlayerOId(request.getPlayerId());
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartedAt(LocalDateTime.now());

        Game saved = gameRepository.save(game);
        log.info("Jugador O se unió: gameId={} playerOId={}", saved.getId(), saved.getPlayerOId());
        return toResponse(saved);
    }

    // ─────────────────────────────────────────────
    // Realizar movimiento
    // ─────────────────────────────────────────────

    @Transactional
    public GameResponse makeMove(Long gameId, MoveRequest request) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        // Regla 3: verificar timeout ANTES de validar el movimiento
        if (game.getStatus() == GameStatus.IN_PROGRESS && validator.isTurnExpired(game)) {
            game = applyTimeout(game);
            return toResponse(game);
        }

        // Reglas 2, 4, 5, 6, 7
        validator.validateMove(game, request.getPlayerId(), request.getPosition());

        // Determinar símbolo del jugador
        PlayerSymbol symbol = game.getPlayerXId().equals(request.getPlayerId())
                ? PlayerSymbol.X : PlayerSymbol.O;

        // Aplicar movimiento al tablero
        String newBoard = validator.applyMove(game.getBoard(), request.getPosition(), symbol);
        game.setBoard(newBoard);
        game.setLastMoveAt(LocalDateTime.now());

        // Registrar el movimiento en el historial
        int moveNumber = gameMoveRepository.countByGameId(gameId) + 1;
        GameMove move = GameMove.builder()
                .gameId(gameId)
                .moveNumber(moveNumber)
                .playerId(request.getPlayerId())
                .symbol(symbol)
                .position(request.getPosition())
                .playedAt(LocalDateTime.now())
                .build();
        gameMoveRepository.save(move);

        // Detectar fin de partida
        GameResult result = validator.detectResult(newBoard);
        if (result != null) {
            game.setStatus(GameStatus.FINISHED);
            game.setWinner(result);
            log.info("Partida finalizada: gameId={} winner={}", gameId, result);
        } else {
            // Cambiar turno
            game.setCurrentTurn(symbol == PlayerSymbol.X ? PlayerSymbol.O : PlayerSymbol.X);
        }

        Game saved = gameRepository.save(game);
        log.info("Movimiento registrado: gameId={} playerId={} pos={} symbol={}", gameId, request.getPlayerId(), request.getPosition(), symbol);
        return toResponse(saved);
    }

    // ─────────────────────────────────────────────
    // Partidas de un jugador en este server
    // ─────────────────────────────────────────────

    @Transactional
    public List<GameResponse> getGamesByPlayer(Long playerId) {
        return gameRepository.findAllByPlayerId(playerId)
                .stream()
                .map(game -> {
                    if (game.getStatus() == GameStatus.IN_PROGRESS && validator.isTurnExpired(game)) {
                        game = applyTimeout(game);
                    }
                    return toResponse(game);
                })
                .toList();
    }

    // ─────────────────────────────────────────────
    // Historial de movimientos de una partida
    // ─────────────────────────────────────────────

    public List<GameMoveResponse> getMoveHistory(Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new GameNotFoundException(gameId);
        }
        return gameMoveRepository.findByGameIdOrderByMoveNumberAsc(gameId).stream()
                .map(m -> GameMoveResponse.builder()
                        .moveNumber(m.getMoveNumber())
                        .playerId(m.getPlayerId())
                        .symbol(m.getSymbol())
                        .position(m.getPosition())
                        .playedAt(m.getPlayedAt())
                        .build())
                .toList();
    }

    // ─────────────────────────────────────────────
    // Helpers privados
    // ─────────────────────────────────────────────

    private Game applyTimeout(Game game) {
        GameResult loser = game.getCurrentTurn() == PlayerSymbol.X ? GameResult.O : GameResult.X;
        // El jugador en turno pierde, el otro gana
        GameResult winner = game.getCurrentTurn() == PlayerSymbol.X ? GameResult.O : GameResult.X;
        game.setStatus(GameStatus.FINISHED);
        game.setWinner(winner);
        log.warn("Timeout aplicado: gameId={} winner={} (turno {} perdió)", game.getId(), winner, game.getCurrentTurn());
        return gameRepository.save(game);
    }

    private GameResponse toResponse(Game game) {
        return GameResponse.builder()
                .id(game.getId())
                .playerXId(game.getPlayerXId())
                .playerOId(game.getPlayerOId())
                .board(game.getBoard())
                .currentTurn(game.getCurrentTurn())
                .status(game.getStatus())
                .winner(game.getWinner())
                .gameType(game.getGameType())
                .turnTimeoutSeconds(game.getTurnTimeoutSeconds())
                .remainingSeconds(validator.getRemainingSeconds(game))
                .createdAt(game.getCreatedAt())
                .startedAt(game.getStartedAt())
                .lastMoveAt(game.getLastMoveAt())
                .build();
    }
}
