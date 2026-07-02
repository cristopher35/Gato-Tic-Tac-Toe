package com.tuapp.gatoserver.dto;

import com.tuapp.gatoserver.model.GameType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreateGameRequest {

    @NotNull(message = "El playerId es obligatorio")
    private Long playerId;

    @NotNull(message = "El tipo de partida es obligatorio (NO_TIME o TIMED)")
    private GameType gameType;

    @Min(value = 10, message = "El timeout mínimo es 10 segundos")
    @Max(value = 300, message = "El timeout máximo es 300 segundos")
    private Integer turnTimeoutSeconds;
}
