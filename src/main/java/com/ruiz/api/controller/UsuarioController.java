package com.ruiz.api.controller;

import com.ruiz.api.dto.UsuarioResponse;
import com.ruiz.api.dto.UsuarioUpdateRequest;
import com.ruiz.api.entity.Usuario.Rol;
import com.ruiz.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios y roles del negocio")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/negocio/{negocioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios del negocio", description = "Solo ADMIN")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    public ResponseEntity<List<UsuarioResponse>> obtenerPorNegocio(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId) {
        return ResponseEntity.ok(usuarioService.obtenerPorNegocio(negocioId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Obtener usuario por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioResponse> obtenerPorId(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(
        summary = "Actualizar nombre, email o contraseña",
        description = "ADMIN puede editar cualquier usuario. EMPLEADO solo puede editarse a sí mismo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UsuarioResponse> actualizar(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PutMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Cambiar rol de un usuario",
        description = "Solo ADMIN. Valores: ADMIN o EMPLEADO"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol actualizado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede cambiar roles")
    })
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Parameter(description = "Nuevo rol: ADMIN o EMPLEADO") @RequestParam Rol rol) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, rol));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Activar o desactivar usuario",
        description = "Solo ADMIN. Útil para suspender acceso sin eliminar el usuario."
    )
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Parameter(description = "true = activo, false = desactivado") @RequestParam boolean activo) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, activo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario", description = "Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede eliminar usuarios")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
