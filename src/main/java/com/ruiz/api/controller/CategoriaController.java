package com.ruiz.api.controller;

import com.ruiz.api.dto.CategoriaDTO;
import com.ruiz.api.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Gestión de categorías de productos por negocio")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping("/negocio/{negocioId}")
    @Operation(summary = "Listar categorías de un negocio")
    @ApiResponse(responseCode = "200", description = "Lista de categorías del negocio")
    public ResponseEntity<List<CategoriaDTO>> obtenerPorNegocio(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId) {
        return ResponseEntity.ok(categoriaService.obtenerPorNegocio(negocioId));
    }

    @PostMapping
    @Operation(summary = "Crear categoría", description = "Crea una nueva categoría asociada a un negocio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Negocio no encontrado")
    })
    public ResponseEntity<CategoriaDTO> crear(@Valid @RequestBody CategoriaDTO request) {
        return new ResponseEntity<>(categoriaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<CategoriaDTO> actualizar(
            @Parameter(description = "ID de la categoría") @PathVariable Long id,
            @Valid @RequestBody CategoriaDTO request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la categoría") @PathVariable Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
