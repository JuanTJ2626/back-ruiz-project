package com.ruiz.api.service;

import com.ruiz.api.dto.ProductoRequest;
import com.ruiz.api.dto.ProductoResponse;

import java.util.List;

public interface ProductoService {

    List<ProductoResponse> obtenerTodos();

    /** Filtra productos por negocio — uso principal en producción. */
    List<ProductoResponse> obtenerPorNegocio(Long negocioId);

    ProductoResponse obtenerPorId(Long id);

    ProductoResponse crear(ProductoRequest request);

    ProductoResponse actualizar(Long id, ProductoRequest request);

    void eliminar(Long id);

    List<ProductoResponse> buscarPorNombre(String nombre);

    /** Busca por nombre dentro de un negocio específico. */
    List<ProductoResponse> buscarPorNombreEnNegocio(String nombre, Long negocioId);

    /** Productos con stock <= stockMinimo para un negocio. */
    List<ProductoResponse> obtenerProductosBajoStock(Long negocioId);

    /** Productos por categoría dentro de un negocio. */
    List<ProductoResponse> obtenerPorCategoria(Long categoriaId, Long negocioId);
}
