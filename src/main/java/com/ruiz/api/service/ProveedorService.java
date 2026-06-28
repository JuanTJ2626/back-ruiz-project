package com.ruiz.api.service;

import com.ruiz.api.dto.ProveedorRequest;
import com.ruiz.api.dto.ProveedorResponse;

import java.util.List;

public interface ProveedorService {

    List<ProveedorResponse> obtenerPorNegocio(Long negocioId);

    ProveedorResponse obtenerPorId(Long id);

    ProveedorResponse crear(ProveedorRequest request);

    ProveedorResponse actualizar(Long id, ProveedorRequest request);

    void eliminar(Long id);

    /** Vincula un proveedor con un producto (relación N:M). */
    ProveedorResponse vincularProducto(Long proveedorId, Long productoId);

    /** Desvincula un proveedor de un producto. */
    ProveedorResponse desvincularProducto(Long proveedorId, Long productoId);
}
