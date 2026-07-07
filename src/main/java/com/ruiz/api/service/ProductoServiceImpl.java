package com.ruiz.api.service;

import com.ruiz.api.dto.ProductoRequest;
import com.ruiz.api.dto.ProductoResponse;
import com.ruiz.api.entity.Categoria;
import com.ruiz.api.entity.Negocio;
import com.ruiz.api.entity.Producto;
import com.ruiz.api.exception.ResourceNotFoundException;
import com.ruiz.api.repository.AtributoProductoRepository;
import com.ruiz.api.repository.CategoriaRepository;
import com.ruiz.api.repository.MovimientoRepository;
import com.ruiz.api.repository.NegocioRepository;
import com.ruiz.api.repository.PedidoProveedorRepository;
import com.ruiz.api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final NegocioRepository negocioRepository;
    private final CategoriaRepository categoriaRepository;
    private final AtributoProductoRepository atributoRepository;
    private final MovimientoRepository movimientoRepository;
    private final PedidoProveedorRepository pedidoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerPorNegocio(Long negocioId) {
        return productoRepository.findByNegocioId(negocioId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        return mapToResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Negocio negocio = negocioRepository.findById(request.getNegocioId())
                .orElseThrow(() -> new ResourceNotFoundException("Negocio", "id", request.getNegocioId()));

        Categoria categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getCategoriaId()));
        }

        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .sku(request.getSku())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .stockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 5)
                .imagenUrl(request.getImagenUrl())
                .negocio(negocio)
                .categoria(categoria)
                .build();

        Producto productoGuardado = productoRepository.save(producto);
        return mapToResponse(productoGuardado);
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        Negocio negocio = negocioRepository.findById(request.getNegocioId())
                .orElseThrow(() -> new ResourceNotFoundException("Negocio", "id", request.getNegocioId()));

        Categoria categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getCategoriaId()));
        }

        producto.setNombre(request.getNombre());
        producto.setSku(request.getSku());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 5);
        producto.setImagenUrl(request.getImagenUrl());
        producto.setNegocio(negocio);
        producto.setCategoria(categoria);

        Producto productoActualizado = productoRepository.save(producto);
        return mapToResponse(productoActualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        // 1. SET NULL en movimientos que referencian este producto
        movimientoRepository.clearProductoId(id);

        // 2. SET NULL en pedidos que referencian este producto
        pedidoRepository.clearProductoId(id);

        // 3. Eliminar atributos del producto (CASCADE)
        atributoRepository.deleteByProductoId(id);

        // 4. Eliminar vinculaciones proveedor-producto (la tabla N:M se limpia automáticamente con JPA)
        productoRepository.delete(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> buscarPorNombreEnNegocio(String nombre, Long negocioId) {
        return productoRepository.findByNombreContainingIgnoreCaseAndNegocioId(nombre, negocioId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerProductosBajoStock(Long negocioId) {
        return productoRepository.findProductosBajoStockPorNegocio(negocioId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> obtenerPorCategoria(Long categoriaId, Long negocioId) {
        return productoRepository.findByCategoriaIdAndNegocioId(categoriaId, negocioId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // Método privado para mapear Entidad -> DTO
    // ============================================
    private ProductoResponse mapToResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .sku(producto.getSku())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .stockMinimo(producto.getStockMinimo())
                .imagenUrl(producto.getImagenUrl())
                .categoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null)
                .categoriaNombre(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null)
                .negocioId(producto.getNegocio() != null ? producto.getNegocio().getId() : null)
                .fechaCreacion(producto.getFechaCreacion())
                .fechaActualizacion(producto.getFechaActualizacion())
                .build();
    }
}
