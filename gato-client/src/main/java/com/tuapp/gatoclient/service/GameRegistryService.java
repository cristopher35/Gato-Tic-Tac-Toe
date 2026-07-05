package com.tuapp.gatoclient.service;

import com.tuapp.gatoclient.exception.ClientException;
import com.tuapp.gatoclient.model.GameRegistry;
import com.tuapp.gatoclient.repository.GameRegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameRegistryService {

    private final GameRegistryRepository gameRegistryRepository;

    @Transactional
    public void register(Long gameId, String serverUrl, Long playerId) {
        GameRegistry registry = GameRegistry.builder()
            .gameId(gameId)
            .serverUrl(serverUrl)
            .playerId(playerId)
            .registeredAt(LocalDateTime.now())
            .build();
        gameRegistryRepository.save(registry);
        log.info("GameRegistry guardado: gameId={} serverUrl={} playerId={}", gameId, serverUrl, playerId);
    }

    public String getServerUrl(Long gameId, Long playerId) {
        return gameRegistryRepository.findByGameIdAndPlayerId(gameId, playerId)
            .map(GameRegistry::getServerUrl)
            .orElseThrow(() -> new ClientException("No se encontró registro para gameId: " + gameId, 404));
    }

    public Map<String, List<GameRegistry>> getGamesByPlayerGroupedByServer(Long playerId) {
        return gameRegistryRepository.findAllByPlayerId(playerId)
            .stream()
            .collect(Collectors.groupingBy(GameRegistry::getServerUrl));
    }
}
