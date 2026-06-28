package com.ruiz.api.controller;

import com.ruiz.api.dto.MovimientoRequest;
import com.ruiz.api.dto.MovimientoResponse;
import com.ruiz.api.service.MovimientoService;
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
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Tag(name = "Control de Stock", description = "Registro de entradas, salidas y ajustes de inventario")
public class MovimientoController {

    private final MovimientoService movimientoService;

    // EMPLEADO y ADMIN pueden registrar movimientos (su función principal)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(
        summary = "Registrar movimiento",
        description = "ADMIN y EMPLEADO pueden registrar movimientos. " +
                      "ENTRADA suma stock, SALIDA resta stock, AJUSTE fija el stock al valor dado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Movimiento registrado y stock actualizado"),
        @ApiResponse(responseCode = "400", description = "Stock insuficiente o datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto o usuario no encontrado")
    })
    public ResponseEntity<MovimientoResponse> registrar(@Valid @RequestBody MovimientoRequest request) {
        return new ResponseEntity<>(movimientoService.registrar(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Obtener movimiento por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento encontrado"),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado")
    })
    public ResponseEntity<MovimientoResponse> obtenerPorId(
            @Parameter(description = "ID del movimiento") @PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.obtenerPorId(id));
    }

    @GetMapping("/producto/{productoId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Historial de movimientos de un producto")
    @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente")
    public ResponseEntity<List<MovimientoResponse>> obtenerPorProducto(
            @Parameter(description = "ID del producto") @PathVariable Long productoId) {
        return ResponseEntity.ok(movimientoService.obtenerPorProducto(productoId));
    }

    @GetMapping("/negocio/{negocioId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO')")
    @Operation(summary = "Últimos movimientos de un negocio")
    @ApiResponse(responseCode = "200", description = "Movimientos obtenidos correctamente")
    public ResponseEntity<List<MovimientoResponse>> obtenerUltimosPorNegocio(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId,
            @RequestParam(defaultValue = "20") int limite) {
        return ResponseEntity.ok(movimientoService.obtenerUltimosPorNegocio(negocioId, limite));
    }
}
