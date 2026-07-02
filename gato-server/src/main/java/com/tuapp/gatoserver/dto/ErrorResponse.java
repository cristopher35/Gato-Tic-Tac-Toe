package com.tuapp.gatoserver.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
    private String error;
    private String message;
}
