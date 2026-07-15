package com.ruiz.api.service;

import com.ruiz.api.dto.NegocioRequest;
import com.ruiz.api.dto.NegocioResponse;
import com.ruiz.api.entity.Negocio;
import com.ruiz.api.entity.Usuario;
import com.ruiz.api.exception.ResourceNotFoundException;
import com.ruiz.api.repository.CategoriaRepository;
import com.ruiz.api.repository.NegocioRepository;
import com.ruiz.api.repository.ProductoRepository;
import com.ruiz.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NegocioServiceImpl implements NegocioService {

    private final NegocioRepository negocioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NegocioResponse> obtenerPorUsuario(Long usuarioId) {
        return negocioRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NegocioResponse obtenerPorId(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio", "id", id));
        return mapToResponse(negocio);
    }

    @Override
    @Transactional
    public NegocioResponse crear(Long usuarioId, NegocioRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        Negocio negocio = Negocio.builder()
                .nombre(request.getNombre())
                .giro(request.getGiro())
                .logoUrl(request.getLogoUrl())
                .usuario(usuario)
                .build();

        return mapToResponse(negocioRepository.save(negocio));
    }

    @Override
    @Transactional
    public NegocioResponse actualizar(Long id, NegocioRequest request) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio", "id", id));

        negocio.setNombre(request.getNombre());
        negocio.setGiro(request.getGiro());
        negocio.setLogoUrl(request.getLogoUrl());

        return mapToResponse(negocioRepository.save(negocio));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Negocio", "id", id));

        // No se puede eliminar un negocio que tiene productos registrados
        long totalProductos = productoRepository.countByNegocioId(id);
        if (totalProductos > 0) {
            throw new IllegalArgumentException(
                "No se puede eliminar el negocio '" + negocio.getNombre() + "' porque tiene " +
                totalProductos + " producto(s) registrado(s). Elimina o reasigna los productos primero."
            );
        }

        // No se puede eliminar un negocio que tiene categorías
        long totalCategorias = categoriaRepository.countByNegocioId(id);
        if (totalCategorias > 0) {
            throw new IllegalArgumentException(
                "No se puede eliminar el negocio '" + negocio.getNombre() + "' porque tiene " +
                totalCategorias + " categoría(s) registrada(s). Elimina las categorías primero."
            );
        }

        // No se puede eliminar un negocio que tiene usuarios asignados
        long totalUsuarios = usuarioRepository.findByNegocioId(id).size();
        if (totalUsuarios > 0) {
            throw new IllegalArgumentException(
                "No se puede eliminar el negocio '" + negocio.getNombre() + "' porque tiene " +
                totalUsuarios + " usuario(s) asignado(s). Desvincula los usuarios primero."
            );
        }

        negocioRepository.delete(negocio);
    }

    // -------------------------------------------------------
    private NegocioResponse mapToResponse(Negocio n) {
        return NegocioResponse.builder()
                .id(n.getId())
                .nombre(n.getNombre())
                .giro(n.getGiro())
                .logoUrl(n.getLogoUrl())
                .usuarioId(n.getUsuario().getId())
                .usuarioNombre(n.getUsuario().getNombre() != null
                        ? n.getUsuario().getNombre()
                        : n.getUsuario().getUsername())
                .fechaCreacion(n.getFechaCreacion())
                .build();
    }
}
