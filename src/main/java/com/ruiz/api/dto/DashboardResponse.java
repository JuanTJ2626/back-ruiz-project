package com.ruiz.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    // Totales generales
    private Integer totalProductos;
    private Integer totalCategorias;
    private Integer totalProveedores;
    private Double valorTotalInventario;

    // Alertas
    private Integer productosStockCritico;      // stock <= stockMinimo
    private Integer productosAgotados;           // stock == 0

    // Listas detalladas
    private List<ProductoResponse> productosBajoStock;
    private List<MovimientoResponse> ultimosMovimientos;
}
