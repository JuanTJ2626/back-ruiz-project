package com.ruiz.api.service;

import com.ruiz.api.dto.NegocioRequest;
import com.ruiz.api.dto.NegocioResponse;

import java.util.List;

public interface NegocioService {

    /** Todos los negocios del usuario autenticado. */
    List<NegocioResponse> obtenerPorUsuario(Long usuarioId);

    NegocioResponse obtenerPorId(Long id);

    /** Crea un nuevo negocio asociado al usuario dado. */
    NegocioResponse crear(Long usuarioId, NegocioRequest request);

    NegocioResponse actualizar(Long id, NegocioRequest request);

    void eliminar(Long id);
}
