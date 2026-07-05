package com.tuapp.gatoclient.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MoveProxyRequest {
    @NotNull(message = "La posición es obligatoria")
    @Min(value = 0, message = "Posición mínima es 0")
    @Max(value = 8, message = "Posición máxima es 8")
    private Integer position;
}
