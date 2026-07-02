package com.tuapp.gatoserver.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(Long gameId) {
        super("Partida no encontrada con ID: " + gameId);
    }
}
