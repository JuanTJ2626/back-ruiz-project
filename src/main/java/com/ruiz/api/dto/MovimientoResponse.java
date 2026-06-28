package com.ruiz.api.dto;

import com.ruiz.api.entity.Movimiento.TipoMovimiento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponse {

    private Long id;
    private TipoMovimiento tipo;
    private Integer cantidad;
    private String motivo;
    private LocalDateTime fecha;
    private Long productoId;
    private String productoNombre;
    private String productoSku;
    private Long usuarioId;
    private String usuarioNombre;
    private Integer stockResultante;
}
