package com.tuapp.gatoclient.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PlayerResponse {
    private Long id;
    private String username;
    private String displayName;
    private LocalDateTime createdAt;
}
