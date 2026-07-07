package com.ruiz.api.repository;

import com.ruiz.api.entity.PedidoProveedor;
import com.ruiz.api.entity.PedidoProveedor.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoProveedorRepository extends JpaRepository<PedidoProveedor, Long> {

    // Todos los pedidos de un proveedor
    List<PedidoProveedor> findByProveedorIdOrderByFechaPedidoDesc(Long proveedorId);

    // Eliminar todos los pedidos de un proveedor (CASCADE al eliminar proveedor)
    void deleteByProveedorId(Long proveedorId);

    // SET NULL en producto (cuando se elimina un producto)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE PedidoProveedor p SET p.producto = null WHERE p.producto.id = :productoId")
    void clearProductoId(@Param("productoId") Long productoId);

    // SET NULL en usuario (cuando se elimina un usuario)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE PedidoProveedor p SET p.usuarioCreador = null WHERE p.usuarioCreador.id = :usuarioId")
    void clearUsuarioId(@Param("usuarioId") Long usuarioId);

    // Pedidos de un negocio completo (via proveedor)
    @Query("SELECT p FROM PedidoProveedor p WHERE p.proveedor.negocio.id = :negocioId ORDER BY p.fechaPedido DESC")
    List<PedidoProveedor> findByNegocioId(@Param("negocioId") Long negocioId);

    // Pedidos pendientes de un negocio (los más importantes para el dashboard)
    @Query("SELECT p FROM PedidoProveedor p WHERE p.proveedor.negocio.id = :negocioId AND p.estado = 'PENDIENTE' ORDER BY p.fechaPedido DESC")
    List<PedidoProveedor> findPendientesByNegocioId(@Param("negocioId") Long negocioId);

    // Pedidos por estado de un negocio
    @Query("SELECT p FROM PedidoProveedor p WHERE p.proveedor.negocio.id = :negocioId AND p.estado = :estado ORDER BY p.fechaPedido DESC")
    List<PedidoProveedor> findByNegocioIdAndEstado(@Param("negocioId") Long negocioId,
                                                    @Param("estado") EstadoPedido estado);

    // Contar pendientes para el dashboard
    @Query("SELECT COUNT(p) FROM PedidoProveedor p WHERE p.proveedor.negocio.id = :negocioId AND p.estado = 'PENDIENTE'")
    long countPendientesByNegocioId(@Param("negocioId") Long negocioId);
}
