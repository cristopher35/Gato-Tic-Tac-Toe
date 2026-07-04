package com.tuapp.gatoserver.service;

import com.tuapp.gatoserver.model.RegisteredClient;
import com.tuapp.gatoserver.repository.RegisteredClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientRegistrationService {

    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Map<String, String> registerClient(String clientId, String clientSecret, String baseUrl) {
        var existing = registeredClientRepository.findByClientId(clientId);

        if (existing.isPresent()) {
            // Ya existe: actualiza baseUrl y retorna 200
            RegisteredClient client = existing.get();
            client.setBaseUrl(baseUrl);
            client.setActive(true);
            registeredClientRepository.save(client);
            log.info("Cliente actualizado: clientId={} baseUrl={}", clientId, baseUrl);
            return Map.of("message", "Cliente actualizado correctamente", "clientId", clientId);
        }

        // Nuevo cliente: hashea el secret y guarda
        RegisteredClient newClient = RegisteredClient.builder()
            .clientId(clientId)
            .clientSecret(passwordEncoder.encode(clientSecret))
            .baseUrl(baseUrl)
            .registeredAt(LocalDateTime.now())
            .active(true)
            .build();

        registeredClientRepository.save(newClient);
        log.info("Cliente registrado: clientId={} baseUrl={}", clientId, baseUrl);
        return Map.of("message", "Cliente registrado correctamente", "clientId", clientId);
    }

    public java.util.List<RegisteredClient> listClients() {
        return registeredClientRepository.findAll();
    }
}
