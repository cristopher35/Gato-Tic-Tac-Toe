package com.tuapp.gatoclient.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ServerHealthResponse {
    private String serverUrl;
    private boolean available;
    private Long latencyMs;
    private LocalDateTime checkedAt;
}
