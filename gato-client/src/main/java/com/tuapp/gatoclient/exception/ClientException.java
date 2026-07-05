package com.tuapp.gatoclient.exception;

public class ClientException extends RuntimeException {
    private final int httpStatus;

    public ClientException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
