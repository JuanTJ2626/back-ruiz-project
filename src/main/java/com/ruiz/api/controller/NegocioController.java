package com.ruiz.api.controller;

import com.ruiz.api.dto.NegocioRequest;
import com.ruiz.api.dto.NegocioResponse;
import com.ruiz.api.service.NegocioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/negocios")
@RequiredArgsConstructor
@Tag(name = "Negocios", description = "Gestión de negocios por usuario (multi-negocio)")
public class NegocioController {

    private final NegocioService negocioService;

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Listar negocios de un usuario")
    @ApiResponse(responseCode = "200", description = "Lista de negocios del usuario")
    public ResponseEntity<List<NegocioResponse>> obtenerPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        return ResponseEntity.ok(negocioService.obtenerPorUsuario(usuarioId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Obtener negocio por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Negocio encontrado"),
        @ApiResponse(responseCode = "404", description = "Negocio no encontrado")
    })
    public ResponseEntity<NegocioResponse> obtenerPorId(
            @Parameter(description = "ID del negocio") @PathVariable Long id) {
        return ResponseEntity.ok(negocioService.obtenerPorId(id));
    }

    @PostMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nuevo negocio", description = "Solo ADMIN puede crear negocios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Negocio creado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede crear negocios")
    })
    public ResponseEntity<NegocioResponse> crear(
            @Parameter(description = "ID del usuario dueño") @PathVariable Long usuarioId,
            @Valid @RequestBody NegocioRequest request) {
        return new ResponseEntity<>(negocioService.crear(usuarioId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar negocio", description = "Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Negocio actualizado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede editar negocios")
    })
    public ResponseEntity<NegocioResponse> actualizar(
            @Parameter(description = "ID del negocio") @PathVariable Long id,
            @Valid @RequestBody NegocioRequest request) {
        return ResponseEntity.ok(negocioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar negocio", description = "Solo ADMIN puede eliminar negocios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Negocio eliminado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede eliminar negocios")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del negocio") @PathVariable Long id) {
        negocioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
