package com.ruiz.api.repository;

import com.ruiz.api.entity.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegocioRepository extends JpaRepository<Negocio, Long> {
    List<Negocio> findByUsuarioId(Long usuarioId);
}
