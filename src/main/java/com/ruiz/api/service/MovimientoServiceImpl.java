package com.ruiz.api.service;

import com.ruiz.api.dto.MovimientoRequest;
import com.ruiz.api.dto.MovimientoResponse;
import com.ruiz.api.entity.Movimiento;
import com.ruiz.api.entity.Movimiento.TipoMovimiento;
import com.ruiz.api.entity.Producto;
import com.ruiz.api.entity.Usuario;
import com.ruiz.api.exception.ResourceNotFoundException;
import com.ruiz.api.repository.MovimientoRepository;
import com.ruiz.api.repository.ProductoRepository;
import com.ruiz.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public MovimientoResponse registrar(MovimientoRequest request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.getProductoId()));

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getUsuarioId()));

        // Calcular nuevo stock según tipo de movimiento
        int stockAnterior = producto.getStock();
        int nuevoStock = calcularNuevoStock(stockAnterior, request.getCantidad(), request.getTipo());

        if (nuevoStock < 0) {
            throw new IllegalArgumentException(
                "Stock insuficiente. Stock actual: " + stockAnterior +
                ", cantidad solicitada: " + request.getCantidad()
            );
        }

        producto.setStock(nuevoStock);
        productoRepository.save(producto);

        // Alerta en log si el stock baja del mínimo
        if (nuevoStock <= producto.getStockMinimo()) {
            log.warn("⚠️  ALERTA STOCK BAJO — Producto: '{}' (ID: {}). Stock: {}, Mínimo: {}",
                    producto.getNombre(), producto.getId(), nuevoStock, producto.getStockMinimo());
        }

        Movimiento movimiento = Movimiento.builder()
                .tipo(request.getTipo())
                .cantidad(request.getCantidad())
                .motivo(request.getMotivo())
                .producto(producto)
                .usuario(usuario)
                .build();

        Movimiento guardado = movimientoRepository.save(movimiento);
        return mapToResponse(guardado, nuevoStock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponse> obtenerPorProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto", "id", productoId);
        }
        return movimientoRepository.findByProductoIdOrderByFechaDesc(productoId)
                .stream()
                .map(m -> mapToResponse(m, m.getProducto().getStock()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoResponse> obtenerUltimosPorNegocio(Long negocioId, int limite) {
        return movimientoRepository.findUltimosMovimientosPorNegocio(negocioId)
                .stream()
                .limit(limite)
                .map(m -> mapToResponse(m, m.getProducto().getStock()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoResponse obtenerPorId(Long id) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento", "id", id));
        return mapToResponse(movimiento, movimiento.getProducto().getStock());
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private int calcularNuevoStock(int stockActual, int cantidad, TipoMovimiento tipo) {
        return switch (tipo) {
            case ENTRADA -> stockActual + cantidad;
            case SALIDA  -> stockActual - cantidad;
            case AJUSTE  -> cantidad; // En AJUSTE, la cantidad ES el nuevo stock total
        };
    }

    private MovimientoResponse mapToResponse(Movimiento m, int stockResultante) {
        return MovimientoResponse.builder()
                .id(m.getId())
                .tipo(m.getTipo())
                .cantidad(m.getCantidad())
                .motivo(m.getMotivo())
                .fecha(m.getFecha())
                .productoId(m.getProducto().getId())
                .productoNombre(m.getProducto().getNombre())
                .productoSku(m.getProducto().getSku())
                .usuarioId(m.getUsuario().getId())
                .usuarioNombre(m.getUsuario().getNombre() != null
                        ? m.getUsuario().getNombre()
                        : m.getUsuario().getUsername())
                .stockResultante(stockResultante)
                .build();
    }
}
