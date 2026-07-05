package com.tuapp.gatoclient.runner;

import com.tuapp.gatoclient.client.GatoServerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientAutoRegistration implements ApplicationRunner {

    private final GatoServerClient serverClient;

    @Value("${client.secret}")
    private String clientSecret;

    @Value("${client.base-url}")
    private String baseUrl;

    // URL del server por defecto - se puede sobrescribir con variable de entorno
    @Value("${server.target-url:http://localhost:8080}")
    private String serverUrl;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Auto-registrando cliente en server: {}", serverUrl);
        serverClient.registerOnServer(serverUrl, clientSecret, baseUrl);
    }
}
