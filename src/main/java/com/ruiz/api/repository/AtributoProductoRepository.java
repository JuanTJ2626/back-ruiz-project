package com.ruiz.api.repository;

import com.ruiz.api.entity.AtributoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtributoProductoRepository extends JpaRepository<AtributoProducto, Long> {

    List<AtributoProducto> findByProductoId(Long productoId);

    Optional<AtributoProducto> findByProductoIdAndClave(Long productoId, String clave);

    boolean existsByProductoIdAndClave(Long productoId, String clave);

    void deleteByProductoId(Long productoId);
}
