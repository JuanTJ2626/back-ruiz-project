package com.ruiz.api.controller;

import com.ruiz.api.dto.ProveedorRequest;
import com.ruiz.api.dto.ProveedorResponse;
import com.ruiz.api.service.ProveedorService;
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
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "Directorio de proveedores y sus productos asociados")
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping("/negocio/{negocioId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO','SUPER_ADMIN')")
    @Operation(summary = "Listar proveedores por negocio")
    @ApiResponse(responseCode = "200", description = "Lista de proveedores del negocio")
    public ResponseEntity<List<ProveedorResponse>> obtenerPorNegocio(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId) {
        return ResponseEntity.ok(proveedorService.obtenerPorNegocio(negocioId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO','SUPER_ADMIN')")
    @Operation(summary = "Obtener proveedor por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proveedor encontrado"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<ProveedorResponse> obtenerPorId(
            @Parameter(description = "ID del proveedor") @PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Crear proveedor", description = "Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Proveedor creado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede crear proveedores")
    })
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        return new ResponseEntity<>(proveedorService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Actualizar proveedor", description = "Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proveedor actualizado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede editar proveedores")
    })
    public ResponseEntity<ProveedorResponse> actualizar(
            @Parameter(description = "ID del proveedor") @PathVariable Long id,
            @Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(proveedorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Eliminar proveedor", description = "Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Proveedor eliminado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede eliminar proveedores")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del proveedor") @PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{proveedorId}/producto/{productoId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Vincular proveedor con producto", description = "Solo ADMIN")
    @ApiResponse(responseCode = "200", description = "Vínculo creado")
    public ResponseEntity<ProveedorResponse> vincularProducto(
            @PathVariable Long proveedorId,
            @PathVariable Long productoId) {
        return ResponseEntity.ok(proveedorService.vincularProducto(proveedorId, productoId));
    }

    @DeleteMapping("/{proveedorId}/producto/{productoId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Desvincular proveedor de producto", description = "Solo ADMIN")
    @ApiResponse(responseCode = "200", description = "Vínculo eliminado")
    public ResponseEntity<ProveedorResponse> desvincularProducto(
            @PathVariable Long proveedorId,
            @PathVariable Long productoId) {
        return ResponseEntity.ok(proveedorService.desvincularProducto(proveedorId, productoId));
    }
}
