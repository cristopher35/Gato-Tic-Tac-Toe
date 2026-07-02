package com.tuapp.gatoserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MoveRequest {

    @NotNull(message = "El playerId es obligatorio")
    private Long playerId;

    @NotNull(message = "La posición es obligatoria")
    @Min(value = 0, message = "La posición mínima es 0")
    @Max(value = 8, message = "La posición máxima es 8")
    private Integer position;
}
