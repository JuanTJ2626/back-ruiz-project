package com.ruiz.api.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.opencsv.CSVWriter;
import com.ruiz.api.dto.DashboardResponse;
import com.ruiz.api.dto.MovimientoResponse;
import com.ruiz.api.dto.ProductoResponse;
import com.ruiz.api.entity.Producto;
import com.ruiz.api.repository.CategoriaRepository;
import com.ruiz.api.repository.MovimientoRepository;
import com.ruiz.api.repository.ProductoRepository;
import com.ruiz.api.repository.ProveedorRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ruiz.api.repository.PedidoProveedorRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int ULTIMOS_MOVIMIENTOS_LIMITE = 10;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final MovimientoRepository movimientoRepository;
    private final PedidoProveedorRepository pedidoProveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtenerDashboard(Long negocioId) {

        List<Producto> bajoStock = productoRepository.findProductosBajoStockPorNegocio(negocioId);
        List<Producto> agotados  = productoRepository.findByNegocioIdAndStock(negocioId, 0);

        List<ProductoResponse> bajoStockDTO = bajoStock.stream()
                .map(this::mapProductoToResponse)
                .collect(Collectors.toList());

        List<MovimientoResponse> ultimosMovimientos = movimientoRepository
                .findUltimosMovimientosPorNegocio(negocioId)
                .stream()
                .limit(ULTIMOS_MOVIMIENTOS_LIMITE)
                .map(m -> MovimientoResponse.builder()
                        .id(m.getId())
                        .tipo(m.getTipo())
                        .cantidad(m.getCantidad())
                        .motivo(m.getMotivo())
                        .fecha(m.getFecha())
                        .productoId(m.getProducto().getId())
                        .productoNombre(m.getProducto().getNombre())
                        .productoSku(m.getProducto().getSku())
                        .usuarioId(m.getUsuario().getId())
                        .usuarioNombre(m.getUsuario().getNombre() != null
                                ? m.getUsuario().getNombre()
                                : m.getUsuario().getUsername())
                        .stockResultante(m.getProducto().getStock())
                        .build())
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalProductos((int) productoRepository.countByNegocioId(negocioId))
                .totalCategorias((int) categoriaRepository.countByNegocioId(negocioId))
                .totalProveedores((int) proveedorRepository.countByNegocioId(negocioId))
                .valorTotalInventario(productoRepository.calcularValorInventarioPorNegocio(negocioId))
                .productosStockCritico(bajoStock.size())
                .productosAgotados(agotados.size())
                .pedidosPendientes(pedidoProveedorRepository.countPendientesByNegocioId(negocioId))
                .productosBajoStock(bajoStockDTO)
                .ultimosMovimientos(ultimosMovimientos)
                .build();
    }

    // -------------------------------------------------------
    // Exportación CSV
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public void exportarCsv(Long negocioId, HttpServletResponse response) {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"inventario_negocio_" + negocioId + ".csv\"");

        List<Producto> productos = productoRepository.findByNegocioId(negocioId);

        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {

            // Encabezado BOM para Excel en español
            response.getOutputStream().write(0xEF);
            response.getOutputStream().write(0xBB);
            response.getOutputStream().write(0xBF);

            // Cabeceras
            writer.writeNext(new String[]{
                "ID", "Nombre", "SKU", "Descripción", "Precio",
                "Stock Actual", "Stock Mínimo", "Categoría",
                "Fecha Creación", "Alerta Stock"
            });

            // Filas
            for (Producto p : productos) {
                writer.writeNext(new String[]{
                    String.valueOf(p.getId()),
                    p.getNombre(),
                    p.getSku() != null ? p.getSku() : "",
                    p.getDescripcion() != null ? p.getDescripcion() : "",
                    String.valueOf(p.getPrecio()),
                    String.valueOf(p.getStock()),
                    String.valueOf(p.getStockMinimo()),
                    p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría",
                    p.getFechaCreacion() != null ? p.getFechaCreacion().format(DATE_FMT) : "",
                    p.getStock() <= p.getStockMinimo() ? "⚠️ BAJO STOCK" : "OK"
                });
            }

        } catch (IOException e) {
            log.error("Error generando CSV para negocio {}: {}", negocioId, e.getMessage());
            throw new RuntimeException("Error al generar el archivo CSV", e);
        }
    }

    // -------------------------------------------------------
    // Exportación PDF
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public void exportarPdf(Long negocioId, HttpServletResponse response) {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"inventario_negocio_" + negocioId + ".pdf\"");

        List<Producto> productos = productoRepository.findByNegocioId(negocioId);

        try {
            PdfWriter pdfWriter = new PdfWriter(response.getOutputStream());
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);

            // Título
            Paragraph titulo = new Paragraph("Reporte de Inventario")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(titulo);

            Paragraph subtitulo = new Paragraph("Negocio ID: " + negocioId +
                    "  |  Total productos: " + productos.size() +
                    "  |  Valor total: $" + String.format("%.2f",
                        productos.stream().mapToDouble(p -> p.getPrecio() * p.getStock()).sum()))
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(15);
            document.add(subtitulo);

            // Tabla
            float[] columnWidths = {1f, 3f, 2f, 2f, 2f, 2f, 2f, 2f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Cabeceras
            String[] headers = {"ID", "Nombre", "SKU", "Precio", "Stock", "Stock Mín.", "Categoría", "Estado"};
            for (String header : headers) {
                Cell headerCell = new Cell()
                        .add(new Paragraph(header).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(headerCell);
            }

            // Filas
            for (Producto p : productos) {
                boolean esStockBajo = p.getStock() <= p.getStockMinimo();

                table.addCell(createCell(String.valueOf(p.getId()), TextAlignment.CENTER));
                table.addCell(createCell(p.getNombre(), TextAlignment.LEFT));
                table.addCell(createCell(p.getSku() != null ? p.getSku() : "-", TextAlignment.CENTER));
                table.addCell(createCell("$" + String.format("%.2f", p.getPrecio()), TextAlignment.RIGHT));
                table.addCell(createCell(String.valueOf(p.getStock()), TextAlignment.CENTER));
                table.addCell(createCell(String.valueOf(p.getStockMinimo()), TextAlignment.CENTER));
                table.addCell(createCell(p.getCategoria() != null ? p.getCategoria().getNombre() : "—", TextAlignment.LEFT));

                Cell estadoCell = createCell(esStockBajo ? "⚠ BAJO" : "OK", TextAlignment.CENTER);
                if (esStockBajo) {
                    estadoCell.setFontColor(ColorConstants.RED).setBold();
                }
                table.addCell(estadoCell);
            }

            document.add(table);
            document.close();

        } catch (IOException e) {
            log.error("Error generando PDF para negocio {}: {}", negocioId, e.getMessage());
            throw new RuntimeException("Error al generar el archivo PDF", e);
        }
    }

    // -------------------------------------------------------
    // Exportación Excel (.xlsx)
    // -------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public void exportarExcel(Long negocioId, HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"inventario_negocio_" + negocioId + ".xlsx\"");

        List<Producto> productos = productoRepository.findByNegocioId(negocioId);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Inventario");

            // ── Estilos ──────────────────────────────────────────
            // Estilo título
            CellStyle estiloTitulo = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteTitulo = workbook.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            estiloTitulo.setFont(fuenteTitulo);
            estiloTitulo.setAlignment(HorizontalAlignment.CENTER);

            // Estilo cabecera (fondo azul oscuro, letra blanca)
            CellStyle estiloCabecera = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteCabecera = workbook.createFont();
            fuenteCabecera.setBold(true);
            fuenteCabecera.setColor(IndexedColors.WHITE.getIndex());
            estiloCabecera.setFont(fuenteCabecera);
            estiloCabecera.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            estiloCabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloCabecera.setAlignment(HorizontalAlignment.CENTER);
            estiloCabecera.setBorderBottom(BorderStyle.THIN);

            // Estilo fila normal
            CellStyle estiloNormal = workbook.createCellStyle();
            estiloNormal.setBorderBottom(BorderStyle.THIN);
            estiloNormal.setBorderLeft(BorderStyle.THIN);
            estiloNormal.setBorderRight(BorderStyle.THIN);

            // Estilo precio (formato moneda)
            CellStyle estiloPrecio = workbook.createCellStyle();
            estiloPrecio.cloneStyleFrom(estiloNormal);
            DataFormat format = workbook.createDataFormat();
            estiloPrecio.setDataFormat(format.getFormat("$#,##0.00"));

            // Estilo stock bajo (fondo rojo claro)
            CellStyle estiloStockBajo = workbook.createCellStyle();
            estiloStockBajo.cloneStyleFrom(estiloNormal);
            estiloStockBajo.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            estiloStockBajo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font fuenteRoja = workbook.createFont();
            fuenteRoja.setBold(true);
            fuenteRoja.setColor(IndexedColors.DARK_RED.getIndex());
            estiloStockBajo.setFont(fuenteRoja);

            // Estilo fila alterna (gris muy claro)
            CellStyle estiloAlterna = workbook.createCellStyle();
            estiloAlterna.cloneStyleFrom(estiloNormal);
            estiloAlterna.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estiloAlterna.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Estilo totales (fondo verde)
            CellStyle estiloTotal = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteTotal = workbook.createFont();
            fuenteTotal.setBold(true);
            estiloTotal.setFont(fuenteTotal);
            estiloTotal.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            estiloTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloTotal.setDataFormat(format.getFormat("$#,##0.00"));

            // ── Fila 0: Título ────────────────────────────────────
            Row filaTitulo = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("Reporte de Inventario — Negocio ID: " + negocioId);
            celdaTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // ── Fila 1: vacía de separación ───────────────────────
            sheet.createRow(1);

            // ── Fila 2: Cabeceras ─────────────────────────────────
            String[] cabeceras = {
                "ID", "Nombre", "SKU", "Descripción", "Precio",
                "Stock Actual", "Stock Mínimo", "Categoría",
                "Fecha Creación", "Estado"
            };
            Row filaCabecera = sheet.createRow(2);
            for (int i = 0; i < cabeceras.length; i++) {
                org.apache.poi.ss.usermodel.Cell celda = filaCabecera.createCell(i);
                celda.setCellValue(cabeceras[i]);
                celda.setCellStyle(estiloCabecera);
            }

            // ── Filas de datos ────────────────────────────────────
            int filaIdx = 3;
            double valorTotal = 0;

            for (Producto p : productos) {
                Row fila = sheet.createRow(filaIdx++);
                boolean esStockBajo = p.getStock() <= p.getStockMinimo();
                CellStyle estiloFila = (filaIdx % 2 == 0) ? estiloAlterna : estiloNormal;

                crearCeldaNum(fila, 0, p.getId(), estiloFila);
                crearCeldaStr(fila, 1, p.getNombre(), estiloFila);
                crearCeldaStr(fila, 2, p.getSku() != null ? p.getSku() : "", estiloFila);
                crearCeldaStr(fila, 3, p.getDescripcion() != null ? p.getDescripcion() : "", estiloFila);

                // Precio con formato moneda
                org.apache.poi.ss.usermodel.Cell celdaPrecio = fila.createCell(4);
                celdaPrecio.setCellValue(p.getPrecio());
                celdaPrecio.setCellStyle(estiloPrecio);

                crearCeldaNum(fila, 5, p.getStock(), estiloFila);
                crearCeldaNum(fila, 6, p.getStockMinimo(), estiloFila);
                crearCeldaStr(fila, 7,
                        p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría",
                        estiloFila);
                crearCeldaStr(fila, 8,
                        p.getFechaCreacion() != null ? p.getFechaCreacion().format(DATE_FMT) : "",
                        estiloFila);

                // Estado con color si es stock bajo
                org.apache.poi.ss.usermodel.Cell celdaEstado = fila.createCell(9);
                celdaEstado.setCellValue(esStockBajo ? "⚠ BAJO STOCK" : "✓ OK");
                celdaEstado.setCellStyle(esStockBajo ? estiloStockBajo : estiloFila);

                valorTotal += p.getPrecio() * p.getStock();
            }

            // ── Fila de totales ───────────────────────────────────
            Row filaTotales = sheet.createRow(filaIdx + 1);
            org.apache.poi.ss.usermodel.Cell lblTotal = filaTotales.createCell(3);
            CellStyle estiloLblTotal = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteLbl = workbook.createFont();
            fuenteLbl.setBold(true);
            estiloLblTotal.setFont(fuenteLbl);
            estiloLblTotal.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            estiloLblTotal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            lblTotal.setCellValue("VALOR TOTAL DEL INVENTARIO:");
            lblTotal.setCellStyle(estiloLblTotal);
            sheet.addMergedRegion(new CellRangeAddress(filaIdx + 1, filaIdx + 1, 3, 3));

            org.apache.poi.ss.usermodel.Cell celdaTotal = filaTotales.createCell(4);
            celdaTotal.setCellValue(valorTotal);
            celdaTotal.setCellStyle(estiloTotal);

            // ── Ajustar ancho de columnas ─────────────────────────
            int[] anchos = {8, 30, 15, 40, 15, 15, 15, 20, 20, 15};
            for (int i = 0; i < anchos.length; i++) {
                sheet.setColumnWidth(i, anchos[i] * 256);
            }

            // Congelar la fila de cabeceras
            sheet.createFreezePane(0, 3);

            workbook.write(response.getOutputStream());

        } catch (IOException e) {
            log.error("Error generando Excel para negocio {}: {}", negocioId, e.getMessage());
            throw new RuntimeException("Error al generar el archivo Excel", e);
        }
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private Cell createCell(String text, TextAlignment alignment) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(9))
                .setTextAlignment(alignment);
    }

    private void crearCeldaStr(Row fila, int col, String valor, CellStyle estilo) {
        org.apache.poi.ss.usermodel.Cell celda = fila.createCell(col);
        celda.setCellValue(valor);
        celda.setCellStyle(estilo);
    }

    private void crearCeldaNum(Row fila, int col, Number valor, CellStyle estilo) {
        org.apache.poi.ss.usermodel.Cell celda = fila.createCell(col);
        celda.setCellValue(valor.doubleValue());
        celda.setCellStyle(estilo);
    }

    private ProductoResponse mapProductoToResponse(Producto p) {
        return ProductoResponse.builder()
                .id(p.getId())
                .nombre(p.getNombre())
                .sku(p.getSku())
                .descripcion(p.getDescripcion())
                .precio(p.getPrecio())
                .stock(p.getStock())
                .stockMinimo(p.getStockMinimo())
                .imagenUrl(p.getImagenUrl())
                .categoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null)
                .categoriaNombre(p.getCategoria() != null ? p.getCategoria().getNombre() : null)
                .negocioId(p.getNegocio() != null ? p.getNegocio().getId() : null)
                .fechaCreacion(p.getFechaCreacion())
                .fechaActualizacion(p.getFechaActualizacion())
                .build();
    }
}
