package com.tuapp.gatoserver.service;

import com.tuapp.gatoserver.exception.InvalidMoveException;
import com.tuapp.gatoserver.model.Game;
import com.tuapp.gatoserver.model.GameResult;
import com.tuapp.gatoserver.model.GameStatus;
import com.tuapp.gatoserver.model.GameType;
import com.tuapp.gatoserver.model.PlayerSymbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GameValidatorServiceTest {

    private GameValidatorService validator;

    // IDs de jugadores para los tests
    private static final Long PLAYER_X_ID = 1L;
    private static final Long PLAYER_O_ID = 2L;
    private static final Long STRANGER_ID  = 99L;

    @BeforeEach
    void setUp() {
        validator = new GameValidatorService();
    }

    // ─────────────────────────────────────────────
    // Helpers para construir partidas de prueba
    // ─────────────────────────────────────────────

    private Game buildInProgressGame(String board, PlayerSymbol currentTurn) {
        return Game.builder()
            .id(1L)
            .playerXId(PLAYER_X_ID)
            .playerOId(PLAYER_O_ID)
            .board(board)
            .currentTurn(currentTurn)
            .status(GameStatus.IN_PROGRESS)
            .gameType(GameType.NO_TIME)
            .createdAt(LocalDateTime.now())
            .startedAt(LocalDateTime.now())
            .build();
    }

    private Game buildTimedGame(String board, PlayerSymbol currentTurn, int timeoutSeconds, LocalDateTime lastMoveAt) {
        return Game.builder()
            .id(1L)
            .playerXId(PLAYER_X_ID)
            .playerOId(PLAYER_O_ID)
            .board(board)
            .currentTurn(currentTurn)
            .status(GameStatus.IN_PROGRESS)
            .gameType(GameType.TIMED)
            .turnTimeoutSeconds(timeoutSeconds)
            .lastMoveAt(lastMoveAt)
            .startedAt(LocalDateTime.now().minusMinutes(1))
            .createdAt(LocalDateTime.now().minusMinutes(1))
            .build();
    }

    // ─────────────────────────────────────────────
    // REGLA 2 — Status IN_PROGRESS
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Regla 2a: Mover en partida WAITING lanza 422")
    void regla2a_moveEnWaiting_lanza422() {
        Game game = buildInProgressGame(".........", PlayerSymbol.X);
        game.setStatus(GameStatus.WAITING);
        game.setPlayerOId(null);

        InvalidMoveException ex = assertThrows(InvalidMoveException.class,
            () -> validator.validateMove(game, PLAYER_X_ID, 0));

        assertEquals(422, ex.getHttpStatus());
    }

    @Test
    @DisplayName("Regla 2b: Mover en partida FINISHED lanza 422")
    void regla2b_moveEnFinished_lanza422() {
        Game game = buildInProgressGame("XOXOXOXOX", PlayerSymbol.X);
        game.setStatus(GameStatus.FINISHED);

        InvalidMoveException ex = assertThrows(InvalidMoveException.class,
            () -> validator.validateMove(game, PLAYER_X_ID, 0));

        assertEquals(422, ex.getHttpStatus());
    }

    // ─────────────────────────────────────────────
    // REGLA 4 — Jugador participante
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Regla 4: Jugador ajeno a la partida lanza 403")
    void regla4_jugadorNoParticipante_lanza403() {
        Game game = buildInProgressGame(".........", PlayerSymbol.X);

        InvalidMoveException ex = assertThrows(InvalidMoveException.class,
            () -> validator.validateMove(game, STRANGER_ID, 0));

        assertEquals(403, ex.getHttpStatus());
    }

    // ─────────────────────────────────────────────
    // REGLA 5 — Turno correcto
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Regla 5: Mover fuera de turno lanza 400")
    void regla5_turnoIncorrecto_lanza400() {
        // Es turno de X, pero mueve O
        Game game = buildInProgressGame(".........", PlayerSymbol.X);

        InvalidMoveException ex = assertThrows(InvalidMoveException.class,
            () -> validator.validateMove(game, PLAYER_O_ID, 0));

        assertEquals(400, ex.getHttpStatus());
    }

    // ─────────────────────────────────────────────
    // REGLA 6 — Posición válida (0-8)
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Regla 6a: Posición -1 lanza 400")
    void regla6a_posicionNegativa_lanza400() {
        Game game = buildInProgressGame(".........", PlayerSymbol.X);

        InvalidMoveException ex = assertThrows(InvalidMoveException.class,
            () -> validator.validateMove(game, PLAYER_X_ID, -1));

        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    @DisplayName("Regla 6b: Posición 9 lanza 400")
    void regla6b_posicion9_lanza400() {
        Game game = buildInProgressGame(".........", PlayerSymbol.X);

        InvalidMoveException ex = assertThrows(InvalidMoveException.class,
            () -> validator.validateMove(game, PLAYER_X_ID, 9));

        assertEquals(400, ex.getHttpStatus());
    }

    // ─────────────────────────────────────────────
    // REGLA 7 — Casilla libre
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Regla 7: Casilla ocupada lanza 400")
    void regla7_casillaOcupada_lanza400() {
        // Posición 0 ya tiene X
        Game game = buildInProgressGame("X........", PlayerSymbol.O);

        InvalidMoveException ex = assertThrows(InvalidMoveException.class,
            () -> validator.validateMove(game, PLAYER_O_ID, 0));

        assertEquals(400, ex.getHttpStatus());
    }

    @Test
    @DisplayName("Regla 7b: Movimiento válido en casilla libre no lanza excepción")
    void regla7b_casillaLibre_noLanzaExcepcion() {
        Game game = buildInProgressGame("X........", PlayerSymbol.O);

        assertDoesNotThrow(() -> validator.validateMove(game, PLAYER_O_ID, 1));
    }

    // ─────────────────────────────────────────────
    // DETECTOR DE VICTORIA — 10 casos
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Victoria X — fila 0 (posiciones 0,1,2)")
    void victoria_X_fila0() {
        assertEquals(GameResult.X, validator.detectResult("XXX......"));
    }

    @Test
    @DisplayName("Victoria X — fila 1 (posiciones 3,4,5)")
    void victoria_X_fila1() {
        assertEquals(GameResult.X, validator.detectResult("...XXX..."));
    }

    @Test
    @DisplayName("Victoria X — fila 2 (posiciones 6,7,8)")
    void victoria_X_fila2() {
        assertEquals(GameResult.X, validator.detectResult("......XXX"));
    }

    @Test
    @DisplayName("Victoria X — columna 0 (posiciones 0,3,6)")
    void victoria_X_columna0() {
        assertEquals(GameResult.X, validator.detectResult("X..X..X.."));
    }

    @Test
    @DisplayName("Victoria X — columna 1 (posiciones 1,4,7)")
    void victoria_X_columna1() {
        assertEquals(GameResult.X, validator.detectResult(".X..X..X."));
    }

    @Test
    @DisplayName("Victoria X — columna 2 (posiciones 2,5,8)")
    void victoria_X_columna2() {
        assertEquals(GameResult.X, validator.detectResult("..X..X..X"));
    }

    @Test
    @DisplayName("Victoria X — diagonal principal (posiciones 0,4,8)")
    void victoria_X_diagonalPrincipal() {
        assertEquals(GameResult.X, validator.detectResult("X...X...X"));
    }

    @Test
    @DisplayName("Victoria X — diagonal secundaria (posiciones 2,4,6)")
    void victoria_X_diagonalSecundaria() {
        assertEquals(GameResult.X, validator.detectResult("..X.X.X.."));
    }

    @Test
    @DisplayName("Empate — tablero lleno sin ganador")
    void empate_tableroLleno() {
        // X O X / O X O / O X O  → sin línea ganadora
        assertEquals(GameResult.DRAW, validator.detectResult("XOXOXOOXO"));
    }

    @Test
    @DisplayName("Sin resultado — tablero con movimientos pero sin ganador aún")
    void sinResultado_partidaEnCurso() {
        assertNull(validator.detectResult("XO......."));
    }

    // ─────────────────────────────────────────────
    // TIMEOUT
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("Timeout: turno expirado retorna true")
    void timeout_turnoExpirado_retornaTrue() {
        // lastMoveAt hace 60 segundos, timeout de 30 segundos → expirado
        LocalDateTime lastMove = LocalDateTime.now().minusSeconds(60);
        Game game = buildTimedGame(".........", PlayerSymbol.X, 30, lastMove);

        assertTrue(validator.isTurnExpired(game));
    }

    @Test
    @DisplayName("Timeout: turno vigente retorna false")
    void timeout_turnoVigente_retornaFalse() {
        // lastMoveAt hace 5 segundos, timeout de 30 segundos → vigente
        LocalDateTime lastMove = LocalDateTime.now().minusSeconds(5);
        Game game = buildTimedGame(".........", PlayerSymbol.X, 30, lastMove);

        assertFalse(validator.isTurnExpired(game));
    }

    @Test
    @DisplayName("Timeout: partida NO_TIME siempre retorna false")
    void timeout_noTime_retornaFalse() {
        Game game = buildInProgressGame(".........", PlayerSymbol.X);
        // Es NO_TIME, no importa cuándo fue el último movimiento
        assertFalse(validator.isTurnExpired(game));
    }
}
