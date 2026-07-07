package com.ruiz.api.repository;

import com.ruiz.api.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByProductoIdOrderByFechaDesc(Long productoId);

    void deleteByProductoId(Long productoId);

    // SET NULL en producto (cuando se elimina un producto)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Movimiento m SET m.producto = null WHERE m.producto.id = :productoId")
    void clearProductoId(@Param("productoId") Long productoId);

    // SET NULL en usuario (cuando se elimina un usuario)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Movimiento m SET m.usuario = null WHERE m.usuario.id = :usuarioId")
    void clearUsuarioId(@Param("usuarioId") Long usuarioId);

    // Últimos N movimientos de un negocio
    @Query("SELECT m FROM Movimiento m WHERE m.producto.negocio.id = :negocioId ORDER BY m.fecha DESC")
    List<Movimiento> findUltimosMovimientosPorNegocio(@Param("negocioId") Long negocioId);

    // Contar movimientos por tipo para un producto
    long countByProductoIdAndTipo(Long productoId, Movimiento.TipoMovimiento tipo);
}
