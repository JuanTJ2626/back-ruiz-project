package com.ruiz.api.service;

import com.ruiz.api.dto.PedidoProveedorRequest;
import com.ruiz.api.dto.PedidoProveedorResponse;
import com.ruiz.api.entity.PedidoProveedor.EstadoPedido;

import java.util.List;

public interface PedidoProveedorService {

    /** Crea un nuevo pedido al proveedor (estado inicial: PENDIENTE). */
    PedidoProveedorResponse crear(PedidoProveedorRequest request);

    /** Actualiza los datos del pedido (descripción, cantidad, fecha esperada, notas). */
    PedidoProveedorResponse actualizar(Long id, PedidoProveedorRequest request);

    /** Cambia el estado del pedido. Al marcarlo RECIBIDO, actualiza automáticamente el stock del producto. */
    PedidoProveedorResponse cambiarEstado(Long id, EstadoPedido nuevoEstado);

    PedidoProveedorResponse obtenerPorId(Long id);

    /** Todos los pedidos de un proveedor específico. */
    List<PedidoProveedorResponse> obtenerPorProveedor(Long proveedorId);

    /** Todos los pedidos del negocio (de todos sus proveedores). */
    List<PedidoProveedorResponse> obtenerPorNegocio(Long negocioId);

    /** Solo pedidos PENDIENTES del negocio. */
    List<PedidoProveedorResponse> obtenerPendientesPorNegocio(Long negocioId);

    /** Pedidos filtrados por estado. */
    List<PedidoProveedorResponse> obtenerPorNegocioYEstado(Long negocioId, EstadoPedido estado);

    void eliminar(Long id);
}
