package com.ruiz.api.dto;

import com.ruiz.api.entity.Movimiento.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoRequest {

    @NotNull(message = "El tipo de movimiento es obligatorio (ENTRADA, SALIDA, AJUSTE)")
    private TipoMovimiento tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Size(max = 255, message = "El motivo no puede exceder los 255 caracteres")
    private String motivo;

    @NotNull(message = "El producto ID es obligatorio")
    private Long productoId;

    @NotNull(message = "El usuario ID es obligatorio")
    private Long usuarioId;
}
