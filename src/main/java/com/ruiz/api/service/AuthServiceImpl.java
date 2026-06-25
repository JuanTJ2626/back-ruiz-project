package com.ruiz.api.service;

import com.ruiz.api.dto.AuthResponse;
import com.ruiz.api.dto.LoginRequest;
import com.ruiz.api.dto.RegisterRequest;
import com.ruiz.api.entity.Usuario;
import com.ruiz.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.mindrot.jbcrypt.BCrypt;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public AuthResponse login(LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(request.getUsername());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // Verificamos la contraseña contra el hash almacenado
            if (BCrypt.checkpw(request.getPassword(), usuario.getPassword())) {
                return AuthResponse.builder()
                        .success(true)
                        .message("Login exitoso")
                        .username(usuario.getUsername())
                        .build();
            }
        }

        return AuthResponse.builder()
                .success(false)
                .message("Usuario o contraseña incorrectos")
                .build();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.builder()
                    .success(false)
                    .message("El usuario ya existe")
                    .build();
        }

        Usuario nuevoUsuario = Usuario.builder()
                .username(request.getUsername())
                .password(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
                .email(request.getEmail())
                .build();

        usuarioRepository.save(nuevoUsuario);

        return AuthResponse.builder()
                .success(true)
                .message("Usuario registrado exitosamente")
                .username(nuevoUsuario.getUsername())
                .build();
    }
}
