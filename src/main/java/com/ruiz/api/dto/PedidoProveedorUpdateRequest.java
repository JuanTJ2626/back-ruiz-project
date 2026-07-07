package com.ruiz.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para actualizar un pedido existente.
 * NO se puede cambiar: proveedor, usuario creador, fecha de pedido.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoProveedorUpdateRequest {

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @Positive(message = "El precio unitario debe ser mayor a 0")
    private Double precioUnitario;

    private LocalDate fechaEsperada;

    @Size(max = 500, message = "Las notas no pueden exceder los 500 caracteres")
    private String notas;

    // Opcional: cambiar el producto vinculado
    private Long productoId;
}
