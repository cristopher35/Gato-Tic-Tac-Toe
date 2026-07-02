package com.tuapp.gatoserver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class JoinGameRequest {

    @NotNull(message = "El playerId es obligatorio")
    private Long playerId;
}
