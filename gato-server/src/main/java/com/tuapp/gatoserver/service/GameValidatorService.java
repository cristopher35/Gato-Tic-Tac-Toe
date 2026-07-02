package com.tuapp.gatoserver.service;

import com.tuapp.gatoserver.exception.InvalidMoveException;
import com.tuapp.gatoserver.model.Game;
import com.tuapp.gatoserver.model.GameResult;
import com.tuapp.gatoserver.model.GameStatus;
import com.tuapp.gatoserver.model.GameType;
import com.tuapp.gatoserver.model.PlayerSymbol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class GameValidatorService {

    // Las 8 combinaciones ganadoras (índices del board 0-8)
    private static final int[][] WINNING_COMBINATIONS = {
        {0, 1, 2}, // fila 0
        {3, 4, 5}, // fila 1
        {6, 7, 8}, // fila 2
        {0, 3, 6}, // columna 0
        {1, 4, 7}, // columna 1
        {2, 5, 8}, // columna 2
        {0, 4, 8}, // diagonal principal
        {2, 4, 6}  // diagonal secundaria
    };

    /**
     * Valida un movimiento en orden estricto (reglas 1-7).
     * Regla 3 (timeout) se delega al TimeoutService antes de llamar aquí.
     * Si pasa todas las validaciones, no lanza excepción.
     *
     * @param game     la partida ya cargada desde BD
     * @param playerId el jugador que intenta mover
     * @param position la posición donde quiere mover (0-8)
     */
    public void validateMove(Game game, Long playerId, int position) {

        // Regla 2: La partida debe estar IN_PROGRESS
        if (game.getStatus() == GameStatus.WAITING) {
            log.warn("Intento de mover en partida WAITING: gameId={} playerId={}", game.getId(), playerId);
            throw new InvalidMoveException(
                "La partida aún no ha comenzado. Espera a que se una un segundo jugador.",
                422
            );
        }
        if (game.getStatus() == GameStatus.FINISHED) {
            log.warn("Intento de mover en partida FINISHED: gameId={} playerId={}", game.getId(), playerId);
            throw new InvalidMoveException(
                "La partida ya ha finalizado.",
                422
            );
        }

        // Regla 4: El jugador debe ser participante (X u O)
        boolean isPlayerX = game.getPlayerXId().equals(playerId);
        boolean isPlayerO = game.getPlayerOId() != null && game.getPlayerOId().equals(playerId);

        if (!isPlayerX && !isPlayerO) {
            log.warn("Jugador no participante intenta mover: gameId={} playerId={}", game.getId(), playerId);
            throw new InvalidMoveException(
                "No eres participante de esta partida.",
                403
            );
        }

        // Regla 5: Es el turno del jugador
        PlayerSymbol playerSymbol = isPlayerX ? PlayerSymbol.X : PlayerSymbol.O;
        if (game.getCurrentTurn() != playerSymbol) {
            log.warn("Turno incorrecto: gameId={} playerId={} turnoActual={}", game.getId(), playerId, game.getCurrentTurn());
            throw new InvalidMoveException(
                "No es tu turno. Turno actual: " + game.getCurrentTurn(),
                400
            );
        }

        // Regla 6: Posición válida (0-8)
        if (position < 0 || position > 8) {
            log.warn("Posición fuera de rango: gameId={} position={}", game.getId(), position);
            throw new InvalidMoveException(
                "Posición inválida: " + position + ". Debe estar entre 0 y 8.",
                400
            );
        }

        // Regla 7: Casilla libre
        char cell = game.getBoard().charAt(position);
        if (cell == 'X' || cell == 'O') {
            log.warn("Casilla ocupada: gameId={} position={} cell={}", game.getId(), position, cell);
            throw new InvalidMoveException(
                "La casilla " + position + " ya está ocupada.",
                400
            );
        }

        log.info("Movimiento válido: gameId={} playerId={} symbol={} position={}", game.getId(), playerId, playerSymbol, position);
    }

    /**
     * Aplica el movimiento al tablero y retorna el board actualizado.
     */
    public String applyMove(String board, int position, PlayerSymbol symbol) {
        char[] cells = board.toCharArray();
        cells[position] = symbol == PlayerSymbol.X ? 'X' : 'O';
        return new String(cells);
    }

    /**
     * Detecta si hay un ganador en el tablero actual.
     *
     * @param board el tablero de 9 chars
     * @return GameResult.X, GameResult.O, GameResult.DRAW, o null si la partida continúa
     */
    public GameResult detectResult(String board) {
        char[] cells = board.toCharArray();

        // Verificar las 8 combinaciones ganadoras
        for (int[] combo : WINNING_COMBINATIONS) {
            char a = cells[combo[0]];
            char b = cells[combo[1]];
            char c = cells[combo[2]];

            if (a != '.' && a == b && b == c) {
                GameResult result = (a == 'X') ? GameResult.X : GameResult.O;
                log.info("Victoria detectada: {} en posiciones [{},{},{}]", result, combo[0], combo[1], combo[2]);
                return result;
            }
        }

        // Verificar empate (todas las casillas ocupadas, sin ganador)
        boolean isFull = board.indexOf('.') == -1;
        if (isFull) {
            log.info("Empate detectado: tablero lleno sin ganador");
            return GameResult.DRAW;
        }

        // La partida continúa
        return null;
    }

    /**
     * Verifica si el turno actual ha expirado (solo para partidas TIMED).
     *
     * @param game la partida
     * @return true si el tiempo expiró
     */
    public boolean isTurnExpired(Game game) {
        if (game.getGameType() != GameType.TIMED) {
            return false;
        }
        if (game.getTurnTimeoutSeconds() == null) {
            return false;
        }

        LocalDateTime turnStartedAt = game.getLastMoveAt() != null
            ? game.getLastMoveAt()
            : game.getStartedAt();

        if (turnStartedAt == null) {
            return false;
        }

        LocalDateTime expiresAt = turnStartedAt.plusSeconds(game.getTurnTimeoutSeconds());
        boolean expired = LocalDateTime.now().isAfter(expiresAt);

        if (expired) {
            log.warn("Timeout detectado: gameId={} turno={} expiraba={}", game.getId(), game.getCurrentTurn(), expiresAt);
        }

        return expired;
    }

    /**
     * Calcula los segundos restantes del turno actual (para incluir en respuesta de estado).
     *
     * @param game la partida
     * @return segundos restantes, o null si no aplica
     */
    public Long getRemainingSeconds(Game game) {
        if (game.getGameType() != GameType.TIMED || game.getTurnTimeoutSeconds() == null) {
            return null;
        }

        LocalDateTime turnStartedAt = game.getLastMoveAt() != null
            ? game.getLastMoveAt()
            : game.getStartedAt();

        if (turnStartedAt == null) {
            return null;
        }

        LocalDateTime expiresAt = turnStartedAt.plusSeconds(game.getTurnTimeoutSeconds());
        long remaining = java.time.Duration.between(LocalDateTime.now(), expiresAt).getSeconds();
        return Math.max(0, remaining);
    }
}
