package com.ruiz.api.service;

import com.ruiz.api.dto.PedidoProveedorRequest;
import com.ruiz.api.dto.PedidoProveedorResponse;
import com.ruiz.api.dto.PedidoProveedorUpdateRequest;
import com.ruiz.api.entity.PedidoProveedor;
import com.ruiz.api.entity.PedidoProveedor.EstadoPedido;
import com.ruiz.api.entity.Producto;
import com.ruiz.api.entity.Proveedor;
import com.ruiz.api.entity.Usuario;
import com.ruiz.api.exception.ResourceNotFoundException;
import com.ruiz.api.repository.PedidoProveedorRepository;
import com.ruiz.api.repository.ProductoRepository;
import com.ruiz.api.repository.ProveedorRepository;
import com.ruiz.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoProveedorServiceImpl implements PedidoProveedorService {

    private final PedidoProveedorRepository pedidoRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public PedidoProveedorResponse crear(PedidoProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", request.getProveedorId()));

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getUsuarioId()));

        Producto producto = null;
        if (request.getProductoId() != null) {
            producto = productoRepository.findById(request.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.getProductoId()));
        }

        PedidoProveedor pedido = PedidoProveedor.builder()
                .descripcion(request.getDescripcion())
                .cantidad(request.getCantidad())
                .precioUnitario(request.getPrecioUnitario())
                .estado(request.getEstado() != null ? request.getEstado() : EstadoPedido.PENDIENTE)
                .fechaEsperada(request.getFechaEsperada())
                .notas(request.getNotas())
                .proveedor(proveedor)
                .producto(producto)
                .usuarioCreador(usuario)
                .build();

        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoProveedorResponse actualizar(Long id, PedidoProveedorUpdateRequest request) {
        PedidoProveedor pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PedidoProveedor", "id", id));

        Producto producto = null;
        if (request.getProductoId() != null) {
            producto = productoRepository.findById(request.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", request.getProductoId()));
        }

        // Actualizar campos editables (NO se cambia el usuario ni el proveedor)
        pedido.setDescripcion(request.getDescripcion());
        pedido.setCantidad(request.getCantidad());
        pedido.setPrecioUnitario(request.getPrecioUnitario());
        pedido.setFechaEsperada(request.getFechaEsperada());
        pedido.setNotas(request.getNotas());
        pedido.setProducto(producto);
        // NO modificar: usuario, proveedor, fechaPedido

        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional
    public PedidoProveedorResponse cambiarEstado(Long id, EstadoPedido nuevoEstado) {
        PedidoProveedor pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PedidoProveedor", "id", id));

        EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(nuevoEstado);

        // Si se marca como RECIBIDO y tiene producto vinculado → suma el stock automáticamente
        if (nuevoEstado == EstadoPedido.RECIBIDO && estadoAnterior != EstadoPedido.RECIBIDO) {
            pedido.setFechaRecibido(LocalDateTime.now());

            if (pedido.getProducto() != null) {
                Producto producto = pedido.getProducto();
                int stockNuevo = producto.getStock() + pedido.getCantidad();
                producto.setStock(stockNuevo);
                productoRepository.save(producto);
                log.info("Stock actualizado por recepción de pedido #{} — Producto: '{}' → nuevo stock: {}",
                        id, producto.getNombre(), stockNuevo);
            }
        }

        return mapToResponse(pedidoRepository.save(pedido));
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoProveedorResponse obtenerPorId(Long id) {
        PedidoProveedor pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PedidoProveedor", "id", id));
        return mapToResponse(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoProveedorResponse> obtenerPorProveedor(Long proveedorId) {
        if (!proveedorRepository.existsById(proveedorId)) {
            throw new ResourceNotFoundException("Proveedor", "id", proveedorId);
        }
        return pedidoRepository.findByProveedorIdOrderByFechaPedidoDesc(proveedorId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoProveedorResponse> obtenerPorNegocio(Long negocioId) {
        return pedidoRepository.findByNegocioId(negocioId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoProveedorResponse> obtenerPendientesPorNegocio(Long negocioId) {
        return pedidoRepository.findPendientesByNegocioId(negocioId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoProveedorResponse> obtenerPorNegocioYEstado(Long negocioId, EstadoPedido estado) {
        return pedidoRepository.findByNegocioIdAndEstado(negocioId, estado)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        PedidoProveedor pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PedidoProveedor", "id", id));
        pedidoRepository.delete(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoProveedor obtenerEntidadPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PedidoProveedor", "id", id));
    }

    // -------------------------------------------------------
    private PedidoProveedorResponse mapToResponse(PedidoProveedor p) {
        Double total = (p.getPrecioUnitario() != null)
                ? p.getPrecioUnitario() * p.getCantidad()
                : null;

        return PedidoProveedorResponse.builder()
                .id(p.getId())
                .descripcion(p.getDescripcion())
                .cantidad(p.getCantidad())
                .precioUnitario(p.getPrecioUnitario())
                .totalPedido(total)
                .estado(p.getEstado())
                .fechaPedido(p.getFechaPedido())
                .fechaEsperada(p.getFechaEsperada())
                .fechaRecibido(p.getFechaRecibido())
                .notas(p.getNotas())
                .proveedorId(p.getProveedor().getId())
                .proveedorNombre(p.getProveedor().getNombre())
                .proveedorTelefono(p.getProveedor().getTelefono())
                .productoId(p.getProducto() != null ? p.getProducto().getId() : null)
                .productoNombre(p.getProducto() != null ? p.getProducto().getNombre() : null)
                .productoSku(p.getProducto() != null ? p.getProducto().getSku() : null)
                .usuarioId(p.getUsuarioCreador().getId())
                .usuarioNombre(p.getUsuarioCreador().getNombre() != null
                        ? p.getUsuarioCreador().getNombre()
                        : p.getUsuarioCreador().getUsername())
                .build();
    }
}
