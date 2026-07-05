package com.tuapp.gatoclient.client;

import com.tuapp.gatoclient.dto.FallbackResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatoServerClientFallbackTest {

    @Mock
    private RestTemplate restTemplate;

    private GatoServerClient gatoServerClient;

    @BeforeEach
    void setUp() throws Exception {
        gatoServerClient = new GatoServerClient("gato-client-1", "secret123");
        // Inyectar el RestTemplate mockeado via reflection
        Field field = GatoServerClient.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(gatoServerClient, restTemplate);
    }

    @Test
    @DisplayName("Fallback POST: timeout retorna 503 con body estructurado")
    void fallback_post_timeout_retorna503() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Object.class)))
            .thenThrow(new ResourceAccessException("Connection timed out"));

        ResponseEntity<Object> response = gatoServerClient.post(
            "http://localhost:8080", "/api/games", null
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertInstanceOf(FallbackResponse.class, response.getBody());
        FallbackResponse body = (FallbackResponse) response.getBody();
        assertEquals("SERVER_UNAVAILABLE", body.getError());
        assertEquals("http://localhost:8080", body.getServerUrl());
        assertNotNull(body.getMessage());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Fallback GET: timeout retorna 503 con body estructurado")
    void fallback_get_timeout_retorna503() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
            .thenThrow(new ResourceAccessException("Connection refused"));

        ResponseEntity<Object> response = gatoServerClient.get(
            "http://localhost:8080", "/api/games/1"
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertInstanceOf(FallbackResponse.class, response.getBody());
        FallbackResponse body = (FallbackResponse) response.getBody();
        assertEquals("SERVER_UNAVAILABLE", body.getError());
    }
}
