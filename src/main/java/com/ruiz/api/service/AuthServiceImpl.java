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
        // Validar username duplicado
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario '" + request.getUsername() + "' ya está en uso.");
        }

        // Validar email duplicado
        if (request.getEmail() != null && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El correo '" + request.getEmail() + "' ya está registrado.");
        }

        // ¿Es una creación desde el panel admin? (viene con negocioId)
        boolean esCreacionPorAdmin = request.getNegocioId() != null;

        // ¿Ya existe un SUPER_ADMIN en el sistema?
        boolean yaTieneSuperAdmin = usuarioRepository.existsByRol(Usuario.Rol.SUPER_ADMIN);

        // Determinar el rol a asignar
        Usuario.Rol rolAsignado;
        if (esCreacionPorAdmin) {
            // Desde el panel admin — no se puede crear otro SUPER_ADMIN
            if (request.getRol() == Usuario.Rol.SUPER_ADMIN) {
                throw new IllegalArgumentException(
                    "No se puede crear otro SUPER_ADMIN. Ya existe uno en el sistema."
                );
            }
            rolAsignado = request.getRol() != null ? request.getRol() : Usuario.Rol.EMPLEADO;
        } else if (!yaTieneSuperAdmin) {
            // Primer registro del sistema → SUPER_ADMIN
            rolAsignado = Usuario.Rol.SUPER_ADMIN;
        } else {
            // Registro público posterior → ADMIN normal
            rolAsignado = Usuario.Rol.ADMIN;
        }

        Usuario nuevoUsuario = Usuario.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nombre(request.getNombre())
                .rol(rolAsignado)
                .activo(true)
                .build();

        usuarioRepository.save(nuevoUsuario);

        Negocio negocio;
        if (esCreacionPorAdmin) {
            // Asociar al negocio existente
            negocio = negocioRepository.findById(request.getNegocioId())
                    .orElseThrow(() -> new RuntimeException(
                        "Negocio no encontrado con id: " + request.getNegocioId()
                    ));
        } else {
            // Crear negocio por defecto para el nuevo usuario
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
                .message(esCreacionPorAdmin ? "Usuario creado exitosamente" : "Usuario registrado exitosamente")
                .id(nuevoUsuario.getId())
                .username(nuevoUsuario.getUsername())
                .token(jwtToken)
                .rol(nuevoUsuario.getRol().name())
                .negocioId(negocio.getId())
                .build();
    }
}
