package com.tuapp.gatoclient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CreateGameProxyRequest {
    @NotBlank(message = "El serverUrl es obligatorio")
    private String serverUrl;
    @NotNull(message = "El gameType es obligatorio")
    private String gameType;
    private Integer turnTimeoutSeconds;
}
