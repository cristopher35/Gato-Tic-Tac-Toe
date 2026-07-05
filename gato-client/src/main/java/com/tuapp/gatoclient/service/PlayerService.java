package com.tuapp.gatoclient.service;

import com.tuapp.gatoclient.dto.PlayerResponse;
import com.tuapp.gatoclient.dto.RegisterPlayerRequest;
import com.tuapp.gatoclient.exception.ClientException;
import com.tuapp.gatoclient.model.Player;
import com.tuapp.gatoclient.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PlayerResponse register(RegisterPlayerRequest request) {
        if (playerRepository.existsByUsername(request.getUsername())) {
            throw new ClientException("Username ya existe: " + request.getUsername(), 409);
        }

        Player player = Player.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .displayName(request.getDisplayName())
            .createdAt(LocalDateTime.now())
            .build();

        Player saved = playerRepository.save(player);
        log.info("Jugador registrado: username={}", saved.getUsername());
        return toResponse(saved);
    }

    public PlayerResponse getPlayer(Long playerId, String authenticatedUsername) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new ClientException("Jugador no encontrado", 404));

        if (!player.getUsername().equals(authenticatedUsername)) {
            throw new ClientException("No puedes consultar datos de otro jugador", 403);
        }

        return toResponse(player);
    }

    private PlayerResponse toResponse(Player player) {
        return PlayerResponse.builder()
            .id(player.getId())
            .username(player.getUsername())
            .displayName(player.getDisplayName())
            .createdAt(player.getCreatedAt())
            .build();
    }
}
