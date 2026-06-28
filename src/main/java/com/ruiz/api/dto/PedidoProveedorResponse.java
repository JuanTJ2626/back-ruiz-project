package com.ruiz.api.dto;

import com.ruiz.api.entity.PedidoProveedor.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoProveedorResponse {

    private Long id;
    private String descripcion;
    private Integer cantidad;
    private Double precioUnitario;
    private Double totalPedido;         // cantidad * precioUnitario
    private EstadoPedido estado;
    private LocalDateTime fechaPedido;
    private LocalDate fechaEsperada;
    private LocalDateTime fechaRecibido;
    private String notas;

    private Long proveedorId;
    private String proveedorNombre;
    private String proveedorTelefono;

    private Long productoId;
    private String productoNombre;
    private String productoSku;

    private Long usuarioId;
    private String usuarioNombre;
}
