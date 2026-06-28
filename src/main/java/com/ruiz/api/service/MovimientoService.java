package com.ruiz.api.service;

import com.ruiz.api.dto.MovimientoRequest;
import com.ruiz.api.dto.MovimientoResponse;

import java.util.List;

public interface MovimientoService {

    /** Registra un movimiento (ENTRADA, SALIDA, AJUSTE) y actualiza el stock del producto. */
    MovimientoResponse registrar(MovimientoRequest request);

    /** Historial de movimientos de un producto específico. */
    List<MovimientoResponse> obtenerPorProducto(Long productoId);

    /** Últimos movimientos de todos los productos de un negocio (para dashboard). */
    List<MovimientoResponse> obtenerUltimosPorNegocio(Long negocioId, int limite);

    /** Detalle de un movimiento por ID. */
    MovimientoResponse obtenerPorId(Long id);
}
