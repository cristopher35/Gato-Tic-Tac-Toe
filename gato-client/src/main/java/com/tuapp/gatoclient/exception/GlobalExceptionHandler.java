package com.tuapp.gatoclient.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<Map<String, String>> handleClientException(ClientException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(
            Map.of("error", "CLIENT_ERROR", "message", ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .findFirst().orElse("Datos inválidos");
        return ResponseEntity.status(400).body(Map.of("error", "VALIDATION_ERROR", "message", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(500).body(
            Map.of("error", "INTERNAL_ERROR", "message", "Error interno. Intenta más tarde.")
        );
    }
}
