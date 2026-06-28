package com.ruiz.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos_proveedor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoProveedor {

    public enum EstadoPedido {
        PENDIENTE,      // recién creado, esperando envío del proveedor
        ENVIADO,        // el proveedor ya despachó
        RECIBIDO,       // el negocio recibió la mercancía
        CANCELADO       // pedido cancelado
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String descripcion;          // qué se pide

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario")
    private Double precioUnitario;       // precio pactado con el proveedor

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "fecha_pedido", nullable = false, updatable = false)
    private LocalDateTime fechaPedido;

    @Column(name = "fecha_esperada")
    private LocalDate fechaEsperada;     // cuándo se espera que llegue

    @Column(name = "fecha_recibido")
    private LocalDateTime fechaRecibido; // cuándo llegó realmente

    @Column(length = 500)
    private String notas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;           // producto que se está pidiendo (opcional)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuarioCreador;      // quién hizo el pedido

    @PrePersist
    protected void onCreate() {
        this.fechaPedido = LocalDateTime.now();
    }
}
