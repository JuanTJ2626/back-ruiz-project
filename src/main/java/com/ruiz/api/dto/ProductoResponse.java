package com.ruiz.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
