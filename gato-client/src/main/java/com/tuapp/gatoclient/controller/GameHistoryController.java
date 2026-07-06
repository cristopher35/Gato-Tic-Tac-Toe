package com.tuapp.gatoclient.controller;

import com.tuapp.gatoclient.service.ServerGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Historial", description = "Historial de movimientos proxeado desde el server")
public class GameHistoryController {

    private final ServerGatewayService gatewayService;

    @Operation(summary = "Historial de movimientos de una partida")
    @GetMapping("/{gameId}/history")
    public ResponseEntity<Object> getHistory(@PathVariable Long gameId) {
        log.info("GET /api/games/{}/history", gameId);
        return ResponseEntity.ok(gatewayService.getGameHistory(gameId));
    }
}
