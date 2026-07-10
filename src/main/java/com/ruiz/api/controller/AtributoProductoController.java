package com.ruiz.api.controller;

import com.ruiz.api.dto.AtributoProductoDTO;
import com.ruiz.api.service.AtributoProductoService;
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
@RequestMapping("/api/productos/{productoId}/atributos")
@RequiredArgsConstructor
@Tag(name = "Atributos de Producto", description = "Atributos personalizados por producto (color, talla, material, peso, etc.)")
public class AtributoProductoController {

    private final AtributoProductoService atributoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO','SUPER_ADMIN')")
    @Operation(
        summary = "Listar atributos de un producto",
        description = "Devuelve todos los atributos personalizados del producto (ej: Color=Rojo, Talla=M)"
    )
    @ApiResponse(responseCode = "200", description = "Lista de atributos")
    public ResponseEntity<List<AtributoProductoDTO>> obtenerPorProducto(
            @Parameter(description = "ID del producto") @PathVariable Long productoId) {
        return ResponseEntity.ok(atributoService.obtenerPorProducto(productoId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
        summary = "Agregar o actualizar un atributo",
        description = "Solo ADMIN. Si la clave ya existe para ese producto, actualiza el valor."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Atributo guardado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<AtributoProductoDTO> guardar(
            @Parameter(description = "ID del producto") @PathVariable Long productoId,
            @Valid @RequestBody AtributoProductoDTO dto) {
        return new ResponseEntity<>(atributoService.guardar(productoId, dto), HttpStatus.CREATED);
    }

    @PostMapping("/lote")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
        summary = "Guardar varios atributos a la vez",
        description = "Solo ADMIN. Útil para enviar todos los atributos al crear un producto."
    )
    @ApiResponse(responseCode = "201", description = "Atributos guardados")
    public ResponseEntity<List<AtributoProductoDTO>> guardarTodos(
            @Parameter(description = "ID del producto") @PathVariable Long productoId,
            @Valid @RequestBody List<AtributoProductoDTO> atributos) {
        return new ResponseEntity<>(atributoService.guardarTodos(productoId, atributos), HttpStatus.CREATED);
    }

    @DeleteMapping("/{atributoId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Eliminar un atributo", description = "Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Atributo eliminado"),
        @ApiResponse(responseCode = "404", description = "Atributo no encontrado")
    })
    public ResponseEntity<Void> eliminar(
            @PathVariable Long productoId,
            @Parameter(description = "ID del atributo") @PathVariable Long atributoId) {
        atributoService.eliminar(atributoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Eliminar todos los atributos del producto", description = "Solo ADMIN")
    @ApiResponse(responseCode = "204", description = "Todos los atributos eliminados")
    public ResponseEntity<Void> eliminarTodos(
            @Parameter(description = "ID del producto") @PathVariable Long productoId) {
        atributoService.eliminarTodos(productoId);
        return ResponseEntity.noContent().build();
    }
}
