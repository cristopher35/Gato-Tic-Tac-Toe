package com.tuapp.gatoclient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RegisterPlayerRequest {
    @NotBlank(message = "El username es obligatorio")
    private String username;
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
    @NotBlank(message = "El displayName es obligatorio")
    private String displayName;
}
