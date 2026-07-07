package com.ruiz.api.repository;

import com.ruiz.api.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar productos por nombre (contiene, ignorando mayúsculas/minúsculas)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Buscar por negocio
    List<Producto> findByNegocioId(Long negocioId);

    // Buscar por nombre dentro de un negocio
    List<Producto> findByNombreContainingIgnoreCaseAndNegocioId(String nombre, Long negocioId);

    // Buscar por categoría dentro de un negocio
    List<Producto> findByCategoriaIdAndNegocioId(Long categoriaId, Long negocioId);

    // SET NULL en categoria (cuando se elimina una categoría)
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Producto p SET p.categoria = null WHERE p.categoria.id = :categoriaId")
    void clearCategoriaId(@Param("categoriaId") Long categoriaId);

    // Productos con stock en nivel crítico (stock <= stockMinimo)
    @Query("SELECT p FROM Producto p WHERE p.negocio.id = :negocioId AND p.stock <= p.stockMinimo")
    List<Producto> findProductosBajoStockPorNegocio(@Param("negocioId") Long negocioId);

    // Productos agotados (stock = 0)
    List<Producto> findByNegocioIdAndStock(Long negocioId, Integer stock);

    // Buscar productos con stock mayor a 0
    List<Producto> findByStockGreaterThan(Integer stock);

    // Verificar si existe un producto con ese nombre
    boolean existsByNombreIgnoreCase(String nombre);

    // Contar productos por negocio
    long countByNegocioId(Long negocioId);

    // Valor total del inventario de un negocio (precio * stock)
    @Query("SELECT COALESCE(SUM(p.precio * p.stock), 0) FROM Producto p WHERE p.negocio.id = :negocioId")
    Double calcularValorInventarioPorNegocio(@Param("negocioId") Long negocioId);
}
