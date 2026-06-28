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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Tag(name = "Control de Stock", description = "Registro de entradas, salidas y ajustes de inventario")
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping
    @Operation(
        summary = "Registrar movimiento",
        description = "Registra una ENTRADA (suma stock), SALIDA (resta stock) o AJUSTE (fija stock al valor dado). Actualiza el stock del producto automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Movimiento registrado y stock actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente para la salida"),
        @ApiResponse(responseCode = "404", description = "Producto o usuario no encontrado")
    })
    public ResponseEntity<MovimientoResponse> registrar(@Valid @RequestBody MovimientoRequest request) {
        return new ResponseEntity<>(movimientoService.registrar(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
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
    @Operation(
        summary = "Historial de movimientos de un producto",
        description = "Devuelve todos los movimientos del producto ordenados del más reciente al más antiguo"
    )
    @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente")
    public ResponseEntity<List<MovimientoResponse>> obtenerPorProducto(
            @Parameter(description = "ID del producto") @PathVariable Long productoId) {
        return ResponseEntity.ok(movimientoService.obtenerPorProducto(productoId));
    }

    @GetMapping("/negocio/{negocioId}")
    @Operation(
        summary = "Últimos movimientos de un negocio",
        description = "Devuelve los últimos movimientos de todos los productos del negocio (máx. 50)"
    )
    @ApiResponse(responseCode = "200", description = "Movimientos obtenidos correctamente")
    public ResponseEntity<List<MovimientoResponse>> obtenerUltimosPorNegocio(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId,
            @Parameter(description = "Número máximo de resultados (default: 20)")
            @RequestParam(defaultValue = "20") int limite) {
        return ResponseEntity.ok(movimientoService.obtenerUltimosPorNegocio(negocioId, limite));
    }
}
