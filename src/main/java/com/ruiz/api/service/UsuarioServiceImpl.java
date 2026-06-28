package com.ruiz.api.service;

import com.ruiz.api.dto.UsuarioResponse;
import com.ruiz.api.dto.UsuarioUpdateRequest;
import com.ruiz.api.entity.Usuario;
import com.ruiz.api.entity.Usuario.Rol;
import com.ruiz.api.exception.ResourceNotFoundException;
import com.ruiz.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerPorNegocio(Long negocioId) {
        return usuarioRepository.findByNegocioId(negocioId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return mapToResponse(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (request.getNombre() != null)
            usuario.setNombre(request.getNombre());

        if (request.getEmail() != null)
            usuario.setEmail(request.getEmail());

        // Solo actualiza contraseña si se mandó
        if (request.getPassword() != null && !request.getPassword().isBlank())
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        return mapToResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarRol(Long id, Rol nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setRol(nuevoRol);
        return mapToResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        usuario.setActivo(activo);
        return mapToResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        usuarioRepository.delete(usuario);
    }

    // -------------------------------------------------------
    private UsuarioResponse mapToResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .nombre(u.getNombre())
                .rol(u.getRol())
                .activo(u.getActivo())
                .negocioId(u.getNegocio() != null ? u.getNegocio().getId() : null)
                .build();
    }
}
