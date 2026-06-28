package com.ruiz.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegocioResponse {

    private Long id;
    private String nombre;
    private String giro;
    private String logoUrl;
    private Long usuarioId;
    private String usuarioNombre;
    private LocalDateTime fechaCreacion;
}
