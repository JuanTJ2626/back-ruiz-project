package com.ruiz.api.dto;

import com.ruiz.api.entity.PedidoProveedor.EstadoPedido;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoProveedorRequest {

    @NotBlank(message = "La descripción del pedido es obligatoria")
    @Size(max = 255)
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    private Double precioUnitario;      // opcional

    private EstadoPedido estado;        // opcional al crear (default PENDIENTE)

    private LocalDate fechaEsperada;    // opcional

    @Size(max = 500)
    private String notas;

    @NotNull(message = "El proveedor ID es obligatorio")
    private Long proveedorId;

    private Long productoId;            // opcional — producto asociado al pedido

    @NotNull(message = "El usuario ID es obligatorio")
    private Long usuarioId;
}
