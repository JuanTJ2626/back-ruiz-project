package com.ruiz.api.service;

import com.ruiz.api.dto.ProductoRequest;
import com.ruiz.api.dto.ProductoResponse;
import com.ruiz.api.entity.Producto;
import com.ruiz.api.exception.ResourceNotFoundException;
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
    public ProductoResponse obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        return mapToResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .build();

        Producto productoGuardado = productoRepository.save(producto);
        return mapToResponse(productoGuardado);
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());

        Producto productoActualizado = productoRepository.save(producto);
        return mapToResponse(productoActualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
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

    // ============================================
    // Método privado para mapear Entidad -> DTO
    // ============================================
    private ProductoResponse mapToResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .fechaCreacion(producto.getFechaCreacion())
                .fechaActualizacion(producto.getFechaActualizacion())
                .build();
    }
}
