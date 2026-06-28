package com.ruiz.api.service;

import com.ruiz.api.dto.AtributoProductoDTO;
import com.ruiz.api.entity.AtributoProducto;
import com.ruiz.api.entity.Producto;
import com.ruiz.api.exception.ResourceNotFoundException;
import com.ruiz.api.repository.AtributoProductoRepository;
import com.ruiz.api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtributoProductoServiceImpl implements AtributoProductoService {

    private final AtributoProductoRepository atributoRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AtributoProductoDTO> obtenerPorProducto(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto", "id", productoId);
        }
        return atributoRepository.findByProductoId(productoId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AtributoProductoDTO guardar(Long productoId, AtributoProductoDTO dto) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productoId));

        // Si la clave ya existe para ese producto, actualiza el valor
        AtributoProducto atributo = atributoRepository
                .findByProductoIdAndClave(productoId, dto.getClave())
                .orElse(AtributoProducto.builder().producto(producto).clave(dto.getClave()).build());

        atributo.setValor(dto.getValor());
        return mapToDTO(atributoRepository.save(atributo));
    }

    @Override
    @Transactional
    public List<AtributoProductoDTO> guardarTodos(Long productoId, List<AtributoProductoDTO> atributos) {
        return atributos.stream()
                .map(dto -> guardar(productoId, dto))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminar(Long atributoId) {
        AtributoProducto atributo = atributoRepository.findById(atributoId)
                .orElseThrow(() -> new ResourceNotFoundException("AtributoProducto", "id", atributoId));
        atributoRepository.delete(atributo);
    }

    @Override
    @Transactional
    public void eliminarTodos(Long productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto", "id", productoId);
        }
        atributoRepository.deleteByProductoId(productoId);
    }

    // -------------------------------------------------------
    private AtributoProductoDTO mapToDTO(AtributoProducto a) {
        return AtributoProductoDTO.builder()
                .id(a.getId())
                .clave(a.getClave())
                .valor(a.getValor())
                .productoId(a.getProducto().getId())
                .build();
    }
}
