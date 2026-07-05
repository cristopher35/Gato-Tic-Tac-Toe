package com.tuapp.gatoclient.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FallbackResponse {
    private String error;
    private String serverUrl;
    private String message;
    private LocalDateTime timestamp;
}
