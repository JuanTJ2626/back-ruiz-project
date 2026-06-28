package com.ruiz.api.service;

import com.ruiz.api.dto.ProveedorRequest;
import com.ruiz.api.dto.ProveedorResponse;
import com.ruiz.api.entity.Negocio;
import com.ruiz.api.entity.Producto;
import com.ruiz.api.entity.Proveedor;
import com.ruiz.api.exception.ResourceNotFoundException;
import com.ruiz.api.repository.NegocioRepository;
import com.ruiz.api.repository.ProductoRepository;
import com.ruiz.api.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final NegocioRepository negocioRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponse> obtenerPorNegocio(Long negocioId) {
        return proveedorRepository.findByNegocioId(negocioId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponse obtenerPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
        return mapToResponse(proveedor);
    }

    @Override
    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        Negocio negocio = negocioRepository.findById(request.getNegocioId())
                .orElseThrow(() -> new ResourceNotFoundException("Negocio", "id", request.getNegocioId()));

        Proveedor proveedor = Proveedor.builder()
                .nombre(request.getNombre())
                .contacto(request.getContacto())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .negocio(negocio)
                .build();

        return mapToResponse(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));

        proveedor.setNombre(request.getNombre());
        proveedor.setContacto(request.getContacto());
        proveedor.setEmail(request.getEmail());
        proveedor.setTelefono(request.getTelefono());

        return mapToResponse(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
        proveedorRepository.delete(proveedor);
    }

    @Override
    @Transactional
    public ProveedorResponse vincularProducto(Long proveedorId, Long productoId) {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", proveedorId));
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productoId));

        proveedor.getProductos().add(producto);
        return mapToResponse(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public ProveedorResponse desvincularProducto(Long proveedorId, Long productoId) {
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", proveedorId));
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productoId));

        proveedor.getProductos().remove(producto);
        return mapToResponse(proveedorRepository.save(proveedor));
    }

    // -------------------------------------------------------
    private ProveedorResponse mapToResponse(Proveedor p) {
        return ProveedorResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .contacto(p.getContacto())
                .email(p.getEmail())
                .telefono(p.getTelefono())
                .negocioId(p.getNegocio().getId())
                .negocioNombre(p.getNegocio().getNombre())
                .build();
    }
}
