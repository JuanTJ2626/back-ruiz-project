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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Endpoints para gestión de productos")
public class ProductoController {

    private final ProductoService productoService;

    // ── Lecturas: ADMIN y EMPLEADO ────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Listar todos los productos")
    @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente")
    public ResponseEntity<List<ProductoResponse>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/negocio/{negocioId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Listar productos por negocio")
    @ApiResponse(responseCode = "200", description = "Lista de productos del negocio")
    public ResponseEntity<List<ProductoResponse>> obtenerPorNegocio(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId) {
        return ResponseEntity.ok(productoService.obtenerPorNegocio(negocioId));
    }

    @GetMapping("/bajo-stock/{negocioId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Productos con stock crítico")
    @ApiResponse(responseCode = "200", description = "Lista de productos con stock bajo")
    public ResponseEntity<List<ProductoResponse>> obtenerBajoStock(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId) {
        return ResponseEntity.ok(productoService.obtenerProductosBajoStock(negocioId));
    }

    @GetMapping("/categoria/{categoriaId}/negocio/{negocioId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Productos por categoría")
    public ResponseEntity<List<ProductoResponse>> obtenerPorCategoria(
            @PathVariable Long categoriaId,
            @PathVariable Long negocioId) {
        return ResponseEntity.ok(productoService.obtenerPorCategoria(categoriaId, negocioId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Obtener producto por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoResponse> obtenerPorId(
            @Parameter(description = "ID del producto") @PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Buscar productos por nombre")
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    public ResponseEntity<List<ProductoResponse>> buscarPorNombre(
            @RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    @GetMapping("/buscar/negocio/{negocioId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Buscar productos por nombre en negocio")
    public ResponseEntity<List<ProductoResponse>> buscarPorNombreEnNegocio(
            @RequestParam String nombre,
            @PathVariable Long negocioId) {
        return ResponseEntity.ok(productoService.buscarPorNombreEnNegocio(nombre, negocioId));
    }

    // ── Endpoint dedicado para el bot (sin JWT, protegido por API key) ────────

    @GetMapping("/bot/{negocioId}")
    @Operation(summary = "Productos para el bot", description = "Autenticado por X-Bot-Key, sin JWT")
    @ApiResponse(responseCode = "200", description = "Lista de productos del negocio")
    public ResponseEntity<Map<String, Object>> productosParaBot(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId,
            @RequestHeader("X-Bot-Key") String botKey) {

        if (!"MI_CLAVE_SECRETA_BOT_123".equals(botKey)) {
            return ResponseEntity.status(403).build();
        }

        List<ProductoResponse> productos = productoService.obtenerPorNegocio(negocioId);
        Map<String, Object> resp = new HashMap<>();
        resp.put("total", productos.size());
        resp.put("productos", productos);
        return ResponseEntity.ok(resp);
    }

    // ── Escritura: solo ADMIN ─────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nuevo producto", description = "Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Solo el ADMIN puede crear productos")
    })
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return new ResponseEntity<>(productoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar producto", description = "Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Solo el ADMIN puede editar productos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar producto", description = "Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
        @ApiResponse(responseCode = "403", description = "Solo el ADMIN puede eliminar productos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
