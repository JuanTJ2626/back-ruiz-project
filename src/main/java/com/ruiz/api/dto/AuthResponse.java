package com.ruiz.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private Long id;           // ← ID del usuario para que el front lo use en peticiones
    private String username;
    private String token;
    private String rol;
    private Long negocioId;
}
