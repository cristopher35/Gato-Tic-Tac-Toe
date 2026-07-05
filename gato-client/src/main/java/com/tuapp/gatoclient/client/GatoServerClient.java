package com.tuapp.gatoclient.client;

import com.tuapp.gatoclient.dto.FallbackResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
public class GatoServerClient {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;

    public GatoServerClient(
            @Value("${client.id}") String clientId,
            @Value("${client.secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
    }

    public ResponseEntity<Object> post(String serverUrl, String path, Object body) {
        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(serverUrl + path, HttpMethod.POST, entity, Object.class);
        } catch (ResourceAccessException e) {
            log.warn("Timeout o conexión rechazada: {} - {}", serverUrl, e.getMessage());
            return buildFallback(serverUrl, 503);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.warn("Error 5xx del server: {} - {}", serverUrl, e.getMessage());
            return buildFallback(serverUrl, 502);
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
            return buildFallback(serverUrl, 503);
        }
    }

    public ResponseEntity<Object> get(String serverUrl, String path) {
        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(serverUrl + path, HttpMethod.GET, entity, Object.class);
        } catch (ResourceAccessException e) {
            log.warn("Timeout o conexión rechazada: {} - {}", serverUrl, e.getMessage());
            return buildFallback(serverUrl, 503);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.warn("Error 5xx del server: {} - {}", serverUrl, e.getMessage());
            return buildFallback(serverUrl, 502);
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage());
            return buildFallback(serverUrl, 503);
        }
    }

    public void registerOnServer(String serverUrl, String clientSecret, String baseUrl) {
        try {
            Map<String, String> body = Map.of(
                "clientId", clientId,
                "clientSecret", clientSecret,
                "baseUrl", baseUrl
            );
            restTemplate.postForObject(serverUrl + "/api/clients/register", body, Object.class);
            log.info("Auto-registrado en server: {}", serverUrl);
        } catch (Exception e) {
            log.warn("No se pudo registrar en el server al arrancar: {} - {}", serverUrl, e.getMessage());
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private ResponseEntity<Object> buildFallback(String serverUrl, int status) {
        FallbackResponse fallback = FallbackResponse.builder()
            .error(status == 503 ? "SERVER_UNAVAILABLE" : "BAD_GATEWAY")
            .serverUrl(serverUrl)
            .message("El servidor de juego no está disponible. Intenta más tarde.")
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(status).body(fallback);
    }
}
