package com.ruiz.api.service;

import com.ruiz.api.config.JwtService;
import com.ruiz.api.dto.AuthResponse;
import com.ruiz.api.dto.LoginRequest;
import com.ruiz.api.dto.RegisterRequest;
import com.ruiz.api.entity.Negocio;
import com.ruiz.api.entity.Usuario;
import com.ruiz.api.repository.NegocioRepository;
import com.ruiz.api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("rol", usuario.getRol().name());
        if (usuario.getNegocio() != null) {
            extraClaims.put("negocioId", usuario.getNegocio().getId());
        }

        String jwtToken = jwtService.generateToken(extraClaims, User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build());

        return AuthResponse.builder()
                .success(true)
                .message("Login exitoso")
                .id(usuario.getId())
                .username(usuario.getUsername())
                .token(jwtToken)
                .rol(usuario.getRol().name())
                .negocioId(usuario.getNegocio() != null ? usuario.getNegocio().getId() : null)
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

        // Si viene negocioId → es un EMPLEADO creado por un ADMIN
        // Si no viene         → es un registro público (nuevo ADMIN con negocio propio)
        boolean esEmpleado = request.getNegocioId() != null;

        Usuario nuevoUsuario = Usuario.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nombre(request.getNombre())
                .rol(esEmpleado
                        ? (request.getRol() != null ? request.getRol() : Usuario.Rol.EMPLEADO)
                        : Usuario.Rol.ADMIN)
                .activo(true)
                .build();

        usuarioRepository.save(nuevoUsuario);

        Negocio negocio;
        if (esEmpleado) {
            // Asociar al negocio existente
            negocio = negocioRepository.findById(request.getNegocioId())
                    .orElseThrow(() -> new RuntimeException("Negocio no encontrado con id: " + request.getNegocioId()));
        } else {
            // Crear negocio por defecto para el nuevo ADMIN
            negocio = Negocio.builder()
                    .nombre("Mi Negocio")
                    .usuario(nuevoUsuario)
                    .build();
            negocioRepository.save(negocio);
        }

        nuevoUsuario.setNegocio(negocio);
        usuarioRepository.save(nuevoUsuario);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("rol", nuevoUsuario.getRol().name());
        extraClaims.put("negocioId", negocio.getId());

        String jwtToken = jwtService.generateToken(extraClaims, User.builder()
                .username(nuevoUsuario.getUsername())
                .password(nuevoUsuario.getPassword())
                .roles(nuevoUsuario.getRol().name())
                .build());

        return AuthResponse.builder()
                .success(true)
                .message(esEmpleado ? "Empleado creado exitosamente" : "Usuario registrado exitosamente")
                .id(nuevoUsuario.getId())
                .username(nuevoUsuario.getUsername())
                .token(jwtToken)
                .rol(nuevoUsuario.getRol().name())
                .negocioId(negocio.getId())
                .build();
    }
}
