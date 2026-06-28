package com.ruiz.api.controller;

import com.ruiz.api.dto.DashboardResponse;
import com.ruiz.api.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard y Reportes", description = "Métricas del inventario y exportación a PDF/CSV")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/negocio/{negocioId}")
    @Operation(
        summary = "Dashboard del inventario",
        description = "Retorna métricas clave: total productos, valor del inventario, productos bajo stock, últimos movimientos"
    )
    @ApiResponse(responseCode = "200", description = "Dashboard generado correctamente")
    public ResponseEntity<DashboardResponse> obtenerDashboard(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId) {
        return ResponseEntity.ok(dashboardService.obtenerDashboard(negocioId));
    }

    @GetMapping("/negocio/{negocioId}/exportar/csv")
    @Operation(
        summary = "Exportar inventario a CSV",
        description = "Descarga un archivo CSV con todos los productos del negocio. Compatible con Excel."
    )
    @ApiResponse(responseCode = "200", description = "Archivo CSV generado")
    public void exportarCsv(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId,
            HttpServletResponse response) {
        dashboardService.exportarCsv(negocioId, response);
    }

    @GetMapping("/negocio/{negocioId}/exportar/pdf")
    @Operation(
        summary = "Exportar inventario a PDF",
        description = "Descarga un archivo PDF con el reporte completo del inventario del negocio"
    )
    @ApiResponse(responseCode = "200", description = "Archivo PDF generado")
    public void exportarPdf(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId,
            HttpServletResponse response) {
        dashboardService.exportarPdf(negocioId, response);
    }

    @GetMapping("/negocio/{negocioId}/exportar/excel")
    @Operation(
        summary = "Exportar inventario a Excel",
        description = "Descarga un archivo .xlsx con el inventario completo: colores, formato moneda, fila de totales y alertas de stock bajo en rojo"
    )
    @ApiResponse(responseCode = "200", description = "Archivo Excel (.xlsx) generado")
    public void exportarExcel(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId,
            HttpServletResponse response) {
        dashboardService.exportarExcel(negocioId, response);
    }
}
