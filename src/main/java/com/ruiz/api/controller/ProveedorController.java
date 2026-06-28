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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "Directorio de proveedores y sus productos asociados")
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping("/negocio/{negocioId}")
    @Operation(summary = "Listar proveedores por negocio")
    @ApiResponse(responseCode = "200", description = "Lista de proveedores del negocio")
    public ResponseEntity<List<ProveedorResponse>> obtenerPorNegocio(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId) {
        return ResponseEntity.ok(proveedorService.obtenerPorNegocio(negocioId));
    }

    @GetMapping("/{id}")
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
    @Operation(summary = "Crear proveedor", description = "Registra un nuevo proveedor para un negocio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Proveedor creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Negocio no encontrado")
    })
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        return new ResponseEntity<>(proveedorService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proveedor actualizado"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<ProveedorResponse> actualizar(
            @Parameter(description = "ID del proveedor") @PathVariable Long id,
            @Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(proveedorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proveedor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Proveedor eliminado"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del proveedor") @PathVariable Long id) {
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{proveedorId}/producto/{productoId}")
    @Operation(
        summary = "Vincular proveedor con producto",
        description = "Asocia un proveedor existente con un producto (relación N:M)"
    )
    @ApiResponse(responseCode = "200", description = "Vínculo creado")
    public ResponseEntity<ProveedorResponse> vincularProducto(
            @Parameter(description = "ID del proveedor") @PathVariable Long proveedorId,
            @Parameter(description = "ID del producto") @PathVariable Long productoId) {
        return ResponseEntity.ok(proveedorService.vincularProducto(proveedorId, productoId));
    }

    @DeleteMapping("/{proveedorId}/producto/{productoId}")
    @Operation(summary = "Desvincular proveedor de producto")
    @ApiResponse(responseCode = "200", description = "Vínculo eliminado")
    public ResponseEntity<ProveedorResponse> desvincularProducto(
            @Parameter(description = "ID del proveedor") @PathVariable Long proveedorId,
            @Parameter(description = "ID del producto") @PathVariable Long productoId) {
        return ResponseEntity.ok(proveedorService.desvincularProducto(proveedorId, productoId));
    }
}
