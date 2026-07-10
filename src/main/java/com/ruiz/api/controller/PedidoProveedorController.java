package com.ruiz.api.controller;

import com.ruiz.api.dto.PedidoProveedorRequest;
import com.ruiz.api.dto.PedidoProveedorResponse;
import com.ruiz.api.dto.PedidoProveedorUpdateRequest;
import com.ruiz.api.entity.PedidoProveedor.EstadoPedido;
import com.ruiz.api.service.EmailService;
import com.ruiz.api.service.PedidoProveedorService;
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
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos a Proveedores", description = "Gestión de órdenes de compra a proveedores y seguimiento de estado")
public class PedidoProveedorController {

    private final PedidoProveedorService pedidoService;
    private final EmailService emailService;

    @GetMapping("/negocio/{negocioId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO','SUPER_ADMIN')")
    @Operation(summary = "Todos los pedidos del negocio")
    @ApiResponse(responseCode = "200", description = "Lista de pedidos")
    public ResponseEntity<List<PedidoProveedorResponse>> obtenerPorNegocio(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId) {
        return ResponseEntity.ok(pedidoService.obtenerPorNegocio(negocioId));
    }

    @GetMapping("/negocio/{negocioId}/pendientes")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO','SUPER_ADMIN')")
    @Operation(
        summary = "Pedidos PENDIENTES del negocio",
        description = "Filtra solo los pedidos que aún no han sido recibidos ni cancelados"
    )
    @ApiResponse(responseCode = "200", description = "Lista de pedidos pendientes")
    public ResponseEntity<List<PedidoProveedorResponse>> obtenerPendientes(
            @Parameter(description = "ID del negocio") @PathVariable Long negocioId) {
        return ResponseEntity.ok(pedidoService.obtenerPendientesPorNegocio(negocioId));
    }

    @GetMapping("/negocio/{negocioId}/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO','SUPER_ADMIN')")
    @Operation(
        summary = "Pedidos por estado",
        description = "Estados posibles: PENDIENTE, ENVIADO, RECIBIDO, CANCELADO"
    )
    public ResponseEntity<List<PedidoProveedorResponse>> obtenerPorEstado(
            @PathVariable Long negocioId,
            @PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.obtenerPorNegocioYEstado(negocioId, estado));
    }

    @GetMapping("/proveedor/{proveedorId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO','SUPER_ADMIN')")
    @Operation(summary = "Pedidos de un proveedor específico")
    public ResponseEntity<List<PedidoProveedorResponse>> obtenerPorProveedor(
            @PathVariable Long proveedorId) {
        return ResponseEntity.ok(pedidoService.obtenerPorProveedor(proveedorId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLEADO','SUPER_ADMIN')")
    @Operation(summary = "Obtener pedido por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<PedidoProveedorResponse> obtenerPorId(
            @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
        summary = "Crear pedido a proveedor",
        description = "Solo ADMIN. Crea un nuevo pedido en estado PENDIENTE. " +
                      "Si se vincula un producto, al marcarlo RECIBIDO el stock se actualiza automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Pedido creado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede crear pedidos")
    })
    public ResponseEntity<PedidoProveedorResponse> crear(
            @Valid @RequestBody PedidoProveedorRequest request) {
        return new ResponseEntity<>(pedidoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Actualizar datos del pedido", description = "Solo ADMIN. No se puede cambiar el proveedor ni el usuario creador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pedido actualizado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    public ResponseEntity<PedidoProveedorResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PedidoProveedorUpdateRequest request) {
        return ResponseEntity.ok(pedidoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
        summary = "Cambiar estado del pedido",
        description = "Solo ADMIN. Al cambiar a RECIBIDO, el stock del producto vinculado se incrementa automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede cambiar el estado")
    })
    public ResponseEntity<PedidoProveedorResponse> cambiarEstado(
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado: PENDIENTE, ENVIADO, RECIBIDO, CANCELADO")
            @RequestParam EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Eliminar pedido", description = "Solo ADMIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Pedido eliminado"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede eliminar pedidos")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enviar-email")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(
        summary = "Enviar email de notificación al proveedor",
        description = "Solo ADMIN. Envía un email al proveedor con los detalles del pedido. " +
                      "El proveedor debe tener un email configurado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email enviado exitosamente"),
        @ApiResponse(responseCode = "400", description = "El proveedor no tiene email configurado"),
        @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error al enviar el email"),
        @ApiResponse(responseCode = "403", description = "Solo ADMIN puede enviar emails")
    })
    public ResponseEntity<String> enviarEmail(@PathVariable Long id) {
        PedidoProveedorResponse pedidoResponse = pedidoService.obtenerPorId(id);
        
        // Convertir response a entity para el email service
        // (En producción, mejor obtener la entidad directamente del service)
        com.ruiz.api.entity.PedidoProveedor pedido = pedidoService.obtenerEntidadPorId(id);
        
        emailService.enviarNotificacionPedido(pedido);
        
        return ResponseEntity.ok("Email enviado exitosamente a " + pedidoResponse.getProveedorNombre());
    }
}

