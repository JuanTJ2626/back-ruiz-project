package com.ruiz.api.service;

import com.ruiz.api.dto.CategoriaDTO;
import java.util.List;

public interface CategoriaService {
    List<CategoriaDTO> obtenerPorNegocio(Long negocioId);
    CategoriaDTO crear(CategoriaDTO request);
    CategoriaDTO actualizar(Long id, CategoriaDTO request);
    void eliminar(Long id);
}
