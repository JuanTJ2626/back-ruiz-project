package com.ruiz.api.service;

import com.ruiz.api.dto.ProductoRequest;
import com.ruiz.api.dto.ProductoResponse;

import java.util.List;

public interface ProductoService {

    List<ProductoResponse> obtenerTodos();

    ProductoResponse obtenerPorId(Long id);

    ProductoResponse crear(ProductoRequest request);

    ProductoResponse actualizar(Long id, ProductoRequest request);

    void eliminar(Long id);

    List<ProductoResponse> buscarPorNombre(String nombre);
}
