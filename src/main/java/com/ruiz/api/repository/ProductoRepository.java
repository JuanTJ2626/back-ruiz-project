package com.ruiz.api.repository;

import com.ruiz.api.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar productos por nombre (contiene, ignorando mayúsculas/minúsculas)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Buscar productos con stock mayor a 0
    List<Producto> findByStockGreaterThan(Integer stock);

    // Verificar si existe un producto con ese nombre
    boolean existsByNombreIgnoreCase(String nombre);
}
