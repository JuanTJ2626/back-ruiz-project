package com.ruiz.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorResponse {

    private Long id;
    private String nombre;
    private String contacto;
    private String email;
    private String telefono;
    private String direccion;
    private Long negocioId;
    private String negocioNombre;
}
