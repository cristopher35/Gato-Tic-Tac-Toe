package com.tuapp.gatoserver.controller;

import com.tuapp.gatoserver.service.ClientRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Clients", description = "Registro de MS Clientes autorizados")
public class ClientController {

    private final ClientRegistrationService clientRegistrationService;

    @Operation(summary = "Registrar MS Cliente (público, llamado al arrancar el cliente)")
    @ApiResponse(responseCode = "200", description = "Cliente registrado o actualizado")
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String clientId = body.get("clientId");
        String clientSecret = body.get("clientSecret");
        String baseUrl = body.get("baseUrl");

        log.info("Registro de cliente: clientId={} baseUrl={}", clientId, baseUrl);
        Map<String, String> result = clientRegistrationService.registerClient(clientId, clientSecret, baseUrl);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Listar MS Clientes registrados (requiere auth de MS)")
    @ApiResponse(responseCode = "200", description = "Lista de clientes")
    @GetMapping
    public ResponseEntity<?> listClients() {
        return ResponseEntity.ok(clientRegistrationService.listClients());
    }
}
