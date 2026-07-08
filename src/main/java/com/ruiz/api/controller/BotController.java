package com.ruiz.api.controller;

import com.ruiz.api.dto.CategoriaDTO;
import com.ruiz.api.dto.NegocioResponse;
import com.ruiz.api.dto.PedidoProveedorResponse;
import com.ruiz.api.dto.ProductoRequest;
import com.ruiz.api.dto.ProductoResponse;
import com.ruiz.api.dto.ProveedorRequest;
import com.ruiz.api.dto.ProveedorResponse;
import com.ruiz.api.service.CategoriaService;
import com.ruiz.api.service.NegocioService;
import com.ruiz.api.service.PedidoProveedorService;
import com.ruiz.api.service.ProductoService;
import com.ruiz.api.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
@Tag(name = "Bot / Resumen", description = "Endpoints para el chatbot — autenticados con X-Bot-Key, sin JWT")
public class BotController {

    private static final String BOT_KEY = "MI_CLAVE_SECRETA_BOT_123";

    private final ProductoService productoService;
    private final NegocioService negocioService;
    private final CategoriaService categoriaService;
    private final ProveedorService proveedorService;
    private final PedidoProveedorService pedidoProveedorService;

    // ─── Validación de clave ────────────────────────────────────────────────
    private boolean claveInvalida(String botKey) {
        return botKey == null || !BOT_KEY.equals(botKey);
    }

    // ─── GET /resumen/{negocioId} ────────────────────────────────────────────
    @GetMapping("/resumen/{negocioId}")
    @Operation(
        summary = "Resumen completo del negocio para el bot",
        description = "Devuelve productos, stock bajo, categorías, proveedores, pedidos pendientes " +
                      "y valor total del inventario. No requiere JWT — usa el header X-Bot-Key."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumen generado correctamente"),
        @ApiResponse(responseCode = "403", description = "X-Bot-Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Negocio no encontrado")
    })
    public ResponseEntity<Map<String, Object>> resumen(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId,
            @Parameter(description = "Clave secreta del bot") @RequestHeader("X-Bot-Key") String botKey) {

        if (claveInvalida(botKey)) return ResponseEntity.status(403).build();

        NegocioResponse negocio                         = negocioService.obtenerPorId(negocioId);
        List<ProductoResponse> productos                = productoService.obtenerPorNegocio(negocioId);
        List<ProductoResponse> bajoStock                = productoService.obtenerProductosBajoStock(negocioId);
        List<CategoriaDTO> categorias                   = categoriaService.obtenerPorNegocio(negocioId);
        List<ProveedorResponse> proveedores             = proveedorService.obtenerPorNegocio(negocioId);
        List<PedidoProveedorResponse> pedidosPendientes = pedidoProveedorService.obtenerPendientesPorNegocio(negocioId);

        double valorTotal = productos.stream()
                .mapToDouble(p -> {
                    double precio = p.getPrecio() != null ? p.getPrecio().doubleValue() : 0.0;
                    int stock     = p.getStock()  != null ? p.getStock()               : 0;
                    return precio * stock;
                })
                .sum();

        Map<String, Object> resp = new HashMap<>();
        resp.put("negocio",                negocio);
        resp.put("totalProductos",          productos.size());
        resp.put("productos",               productos);
        resp.put("productosBajoStock",      bajoStock);
        resp.put("totalCategorias",         categorias.size());
        resp.put("categorias",              categorias);
        resp.put("totalProveedores",        proveedores.size());
        resp.put("proveedores",             proveedores);
        resp.put("pedidosPendientes",       pedidosPendientes.size());
        resp.put("listaPedidosPendientes",  pedidosPendientes);
        resp.put("valorTotalInventario",    valorTotal);

        return ResponseEntity.ok(resp);
    }

    // ─── POST /productos ─────────────────────────────────────────────────────
    @PostMapping("/productos")
    @Operation(summary = "Crear producto desde el bot", description = "No requiere JWT — usa X-Bot-Key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado correctamente"),
        @ApiResponse(responseCode = "403", description = "X-Bot-Key inválida o ausente")
    })
    public ResponseEntity<ProductoResponse> crearProducto(
            @RequestHeader("X-Bot-Key") String botKey,
            @RequestBody ProductoRequest request) {

        if (claveInvalida(botKey)) return ResponseEntity.status(403).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    // ─── PUT /productos/{id} ──────────────────────────────────────────────────
    @PutMapping("/productos/{id}")
    @Operation(summary = "Actualizar producto desde el bot", description = "No requiere JWT — usa X-Bot-Key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
        @ApiResponse(responseCode = "403", description = "X-Bot-Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @RequestHeader("X-Bot-Key") String botKey,
            @PathVariable Long id,
            @RequestBody ProductoRequest request) {

        if (claveInvalida(botKey)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    // ─── DELETE /productos/{id} ───────────────────────────────────────────────
    @DeleteMapping("/productos/{id}")
    @Operation(summary = "Eliminar producto desde el bot", description = "No requiere JWT — usa X-Bot-Key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente"),
        @ApiResponse(responseCode = "403", description = "X-Bot-Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> eliminarProducto(
            @RequestHeader("X-Bot-Key") String botKey,
            @PathVariable Long id) {

        if (claveInvalida(botKey)) return ResponseEntity.status(403).build();
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ─── POST /proveedores ───────────────────────────────────────────────────
    @PostMapping("/proveedores")
    @Operation(summary = "Crear proveedor desde el bot", description = "No requiere JWT — usa X-Bot-Key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Proveedor creado correctamente"),
        @ApiResponse(responseCode = "403", description = "X-Bot-Key inválida o ausente")
    })
    public ResponseEntity<ProveedorResponse> crearProveedor(
            @RequestHeader("X-Bot-Key") String botKey,
            @RequestBody ProveedorRequest request) {

        if (claveInvalida(botKey)) return ResponseEntity.status(403).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crear(request));
    }

    // ─── PUT /proveedores/{id} ────────────────────────────────────────────────
    @PutMapping("/proveedores/{id}")
    @Operation(summary = "Actualizar proveedor desde el bot", description = "No requiere JWT — usa X-Bot-Key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Proveedor actualizado correctamente"),
        @ApiResponse(responseCode = "403", description = "X-Bot-Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<ProveedorResponse> actualizarProveedor(
            @RequestHeader("X-Bot-Key") String botKey,
            @PathVariable Long id,
            @RequestBody ProveedorRequest request) {

        if (claveInvalida(botKey)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(proveedorService.actualizar(id, request));
    }

    // ─── DELETE /proveedores/{id} ─────────────────────────────────────────────
    @DeleteMapping("/proveedores/{id}")
    @Operation(summary = "Eliminar proveedor desde el bot", description = "No requiere JWT — usa X-Bot-Key.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Proveedor eliminado correctamente"),
        @ApiResponse(responseCode = "403", description = "X-Bot-Key inválida o ausente"),
        @ApiResponse(responseCode = "404", description = "Proveedor no encontrado")
    })
    public ResponseEntity<Void> eliminarProveedor(
            @RequestHeader("X-Bot-Key") String botKey,
            @PathVariable Long id) {

        if (claveInvalida(botKey)) return ResponseEntity.status(403).build();
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ─── POST /categorias ────────────────────────────────────────────────────
    @PostMapping("/categorias")
    @Operation(
        summary = "Crear categoría desde el bot",
        description = "Crea una categoría en el negocio indicado. No requiere JWT — usa X-Bot-Key."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada correctamente"),
        @ApiResponse(responseCode = "403", description = "X-Bot-Key inválida o ausente")
    })
    public ResponseEntity<CategoriaDTO> crearCategoria(
            @RequestHeader("X-Bot-Key") String botKey,
            @RequestBody CategoriaDTO request) {

        if (claveInvalida(botKey)) return ResponseEntity.status(403).build();

        CategoriaDTO creada = categoriaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }
}
