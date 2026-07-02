package com.tuapp.gatoserver.exception;

public class InvalidMoveException extends RuntimeException {
    private final int httpStatus;

    public InvalidMoveException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
