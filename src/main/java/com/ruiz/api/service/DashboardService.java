package com.ruiz.api.service;

import com.ruiz.api.dto.DashboardResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface DashboardService {

    /** Métricas y resumen del inventario para un negocio. */
    DashboardResponse obtenerDashboard(Long negocioId);

    /** Exporta el inventario completo del negocio a CSV. */
    void exportarCsv(Long negocioId, HttpServletResponse response);

    /** Exporta el inventario completo del negocio a PDF. */
    void exportarPdf(Long negocioId, HttpServletResponse response);

    /** Exporta el inventario completo del negocio a Excel (.xlsx). */
    void exportarExcel(Long negocioId, HttpServletResponse response);
}
