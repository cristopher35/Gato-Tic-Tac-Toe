package com.tuapp.gatoclient.controller;

import com.tuapp.gatoclient.dto.PlayerResponse;
import com.tuapp.gatoclient.dto.RegisterPlayerRequest;
import com.tuapp.gatoclient.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Players", description = "Gestión de jugadores")
public class PlayerController {

    private final PlayerService playerService;

    @Operation(summary = "Registrar nuevo jugador (público)")
    @PostMapping
    public ResponseEntity<PlayerResponse> register(@Valid @RequestBody RegisterPlayerRequest request) {
        log.info("POST /api/players - username={}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.register(request));
    }

    @Operation(summary = "Obtener datos del jugador autenticado")
    @GetMapping("/{playerId}")
    public ResponseEntity<PlayerResponse> getPlayer(
            @PathVariable Long playerId,
            Authentication auth) {
        return ResponseEntity.ok(playerService.getPlayer(playerId, auth.getName()));
    }
}
