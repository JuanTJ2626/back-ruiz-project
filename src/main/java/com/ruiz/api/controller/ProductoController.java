package com.ruiz.api.controller;

import com.ruiz.api.dto.ProductoRequest;
import com.ruiz.api.dto.ProductoResponse;
import com.ruiz.api.service.ProductoService;
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
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Endpoints para gestión de productos")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar todos los productos", description = "Retorna la lista completa de productos registrados")
    @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente")
    public ResponseEntity<List<ProductoResponse>> obtenerTodos() {
        List<ProductoResponse> productos = productoService.obtenerTodos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Retorna un producto específico basado en su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoResponse> obtenerPorId(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        ProductoResponse producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo producto", description = "Registra un nuevo producto en la base de datos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<ProductoResponse> crear(
            @Valid @RequestBody ProductoRequest request) {
        ProductoResponse producto = productoService.crear(request);
        return new ResponseEntity<>(producto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza los datos de un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<ProductoResponse> actualizar(
            @Parameter(description = "ID del producto") @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        ProductoResponse producto = productoService.actualizar(id, request);
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto de la base de datos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar productos por nombre", description = "Busca productos cuyo nombre contenga el texto especificado")
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    public ResponseEntity<List<ProductoResponse>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre") @RequestParam String nombre) {
        List<ProductoResponse> productos = productoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(productos);
    }
}
