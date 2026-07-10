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

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Listar todos los usuarios", description = "ADMIN y SUPER_ADMIN")
    @ApiResponse(responseCode = "200", description = "Lista completa de usuarios")
    public ResponseEntity<List<UsuarioResponse>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/negocio/{negocioId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Listar usuarios del negocio", description = "ADMIN y SUPER_ADMIN")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios")
    public ResponseEntity<List<UsuarioResponse>> obtenerPorNegocio(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId) {
        return ResponseEntity.ok(usuarioService.obtenerPorNegocio(negocioId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO','SUPER_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO','SUPER_ADMIN')")
    @Operation(
        summary = "Actualizar nombre, email o contraseña",
        description = "SUPER_ADMIN y ADMIN pueden editar cualquier usuario. EMPLEADO solo puede editarse a sí mismo."
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
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
        summary = "Cambiar rol de un usuario",
        description = "ADMIN: EMPLEADO↔ADMIN. SUPER_ADMIN: puede asignar cualquier rol."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rol actualizado"),
        @ApiResponse(responseCode = "400", description = "No se puede cambiar el rol del SUPER_ADMIN")
    })
    public ResponseEntity<UsuarioResponse> cambiarRol(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Parameter(description = "Nuevo rol: SUPER_ADMIN, ADMIN o EMPLEADO") @RequestParam Rol rol) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, rol));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
        summary = "Activar o desactivar usuario",
        description = "ADMIN y SUPER_ADMIN. Suspende el acceso sin eliminar la cuenta."
    )
    @ApiResponse(responseCode = "200", description = "Estado actualizado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Parameter(description = "true = activo, false = desactivado") @RequestParam boolean activo) {
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, activo));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
        summary = "Eliminar usuario",
        description = "ADMIN elimina EMPLEADOs. SUPER_ADMIN elimina cualquier cuenta excepto la suya propia."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado"),
        @ApiResponse(responseCode = "400", description = "No se puede eliminar al SUPER_ADMIN")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/negocio")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
        summary = "Asignar negocio a un usuario",
        description = "ADMIN y SUPER_ADMIN. Asigna o cambia el negocio de un empleado."
    )
    @ApiResponse(responseCode = "200", description = "Negocio asignado correctamente")
    public ResponseEntity<UsuarioResponse> asignarNegocio(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Parameter(description = "ID del negocio a asignar") @RequestParam Long negocioId) {
        return ResponseEntity.ok(usuarioService.asignarNegocio(id, negocioId));
    }

    @DeleteMapping("/{id}/negocio")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
        summary = "Quitar negocio de un usuario",
        description = "ADMIN y SUPER_ADMIN. Deja al usuario sin negocio asignado."
    )
    @ApiResponse(responseCode = "200", description = "Negocio quitado correctamente")
    public ResponseEntity<UsuarioResponse> quitarNegocio(
            @Parameter(description = "ID del usuario") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.quitarNegocio(id));
    }
}
