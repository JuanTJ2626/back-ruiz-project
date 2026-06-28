package com.ruiz.api.service;

import com.ruiz.api.dto.CategoriaDTO;
import com.ruiz.api.entity.Categoria;
import com.ruiz.api.entity.Negocio;
import com.ruiz.api.exception.ResourceNotFoundException;
import com.ruiz.api.repository.CategoriaRepository;
import com.ruiz.api.repository.NegocioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final NegocioRepository negocioRepository;

    @Override
    public List<CategoriaDTO> obtenerPorNegocio(Long negocioId) {
        return categoriaRepository.findByNegocioId(negocioId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoriaDTO crear(CategoriaDTO request) {
        Negocio negocio = negocioRepository.findById(request.getNegocioId())
                .orElseThrow(() -> new ResourceNotFoundException("Negocio", "id", request.getNegocioId()));

        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .negocio(negocio)
                .build();

        return mapToDTO(categoriaRepository.save(categoria));
    }

    @Override
    public CategoriaDTO actualizar(Long id, CategoriaDTO request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", id));

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        return mapToDTO(categoriaRepository.save(categoria));
    }

    @Override
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", id));
        categoriaRepository.delete(categoria);
    }

    private CategoriaDTO mapToDTO(Categoria categoria) {
        return CategoriaDTO.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .negocioId(categoria.getNegocio().getId())
                .build();
    }
}
