package com.ruiz.api.service;

import com.ruiz.api.dto.AtributoProductoDTO;

import java.util.List;

public interface AtributoProductoService {

    /** Todos los atributos de un producto. */
    List<AtributoProductoDTO> obtenerPorProducto(Long productoId);

    /** Agrega o actualiza un atributo (si la clave ya existe, la sobreescribe). */
    AtributoProductoDTO guardar(Long productoId, AtributoProductoDTO dto);

    /** Guarda una lista de atributos de golpe (útil al crear el producto). */
    List<AtributoProductoDTO> guardarTodos(Long productoId, List<AtributoProductoDTO> atributos);

    /** Elimina un atributo por su ID. */
    void eliminar(Long atributoId);

    /** Elimina todos los atributos de un producto. */
    void eliminarTodos(Long productoId);
}
