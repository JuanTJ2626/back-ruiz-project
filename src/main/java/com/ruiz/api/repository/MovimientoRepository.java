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

    // Últimos N movimientos de un negocio
    @Query("SELECT m FROM Movimiento m WHERE m.producto.negocio.id = :negocioId ORDER BY m.fecha DESC")
    List<Movimiento> findUltimosMovimientosPorNegocio(@Param("negocioId") Long negocioId);

    // Contar movimientos por tipo para un producto
    long countByProductoIdAndTipo(Long productoId, Movimiento.TipoMovimiento tipo);
}
