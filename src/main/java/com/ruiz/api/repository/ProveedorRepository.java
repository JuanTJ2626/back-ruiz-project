package com.ruiz.api.repository;

import com.ruiz.api.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findByNegocioId(Long negocioId);

    boolean existsByNombreIgnoreCaseAndNegocioId(String nombre, Long negocioId);

    long countByNegocioId(Long negocioId);
}
