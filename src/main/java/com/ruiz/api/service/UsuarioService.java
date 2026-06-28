package com.ruiz.api.service;

import com.ruiz.api.dto.UsuarioResponse;
import com.ruiz.api.dto.UsuarioUpdateRequest;
import com.ruiz.api.entity.Usuario.Rol;

import java.util.List;

public interface UsuarioService {

    /** Listar todos los usuarios de un negocio. */
    List<UsuarioResponse> obtenerPorNegocio(Long negocioId);

    /** Obtener un usuario por ID. */
    UsuarioResponse obtenerPorId(Long id);

    /** Actualizar nombre, email o contraseña del usuario. */
    UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request);

    /** Cambiar el rol del usuario (ADMIN o EMPLEADO). */
    UsuarioResponse cambiarRol(Long id, Rol nuevoRol);

    /** Activar o desactivar un usuario sin eliminarlo. */
    UsuarioResponse cambiarEstado(Long id, boolean activo);

    /** Eliminar usuario permanentemente. */
    void eliminar(Long id);
}
