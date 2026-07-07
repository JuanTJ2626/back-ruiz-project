package com.ruiz.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruiz.api.entity.PedidoProveedor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${app.mail.from:noreply@reporteurbano.site}")
    private String fromEmail;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void enviarNotificacionPedido(PedidoProveedor pedido) {
        if (pedido.getProveedor().getEmail() == null || pedido.getProveedor().getEmail().isBlank()) {
            log.warn("El proveedor {} no tiene email configurado", pedido.getProveedor().getNombre());
            throw new IllegalArgumentException("El proveedor no tiene email configurado");
        }

        try {
            String toEmail       = pedido.getProveedor().getEmail();
            String negocioNombre = pedido.getProveedor().getNegocio().getNombre();
            String htmlContent   = construirEmailHTML(pedido);

            Map<String, Object> body = Map.of(
                "from",    "Pedidos " + negocioNombre + " <" + fromEmail + ">",
                "to",      List.of(toEmail),
                "subject", "Nuevo pedido #" + pedido.getId() + " - " + negocioNombre,
                "html",    htmlContent
            );

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                log.info("Email enviado via Resend al proveedor: {} ({})",
                        pedido.getProveedor().getNombre(), toEmail);
            } else {
                log.error("Resend error. Status: {}, Body: {}", response.statusCode(), response.body());
                throw new RuntimeException("Resend error " + response.statusCode() + ": " + response.body());
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al enviar email al proveedor {}: {}",
                    pedido.getProveedor().getNombre(), e.getMessage(), e);
            throw new RuntimeException("Error al enviar el email: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Template HTML moderno
    // ─────────────────────────────────────────────────────────────────────────

    private String construirEmailHTML(PedidoProveedor pedido) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy, HH:mm",
                java.util.Locale.forLanguageTag("es"));
        DateTimeFormatter df  = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy",
                java.util.Locale.forLanguageTag("es"));

        String negocioNombre   = pedido.getProveedor().getNegocio().getNombre();
        String proveedorNombre = pedido.getProveedor().getNombre();
        String contacto        = nvl(pedido.getProveedor().getContacto(), "—");
        String telefono        = nvl(pedido.getProveedor().getTelefono(), "—");
        String producto        = pedido.getProducto() != null ? pedido.getProducto().getNombre() : "No especificado";
        String descripcion     = pedido.getDescripcion();
        int    cantidad        = pedido.getCantidad();
        Double precioUnit      = pedido.getPrecioUnitario();
        Double total           = precioUnit != null ? precioUnit * cantidad : null;
        String estado          = pedido.getEstado().toString();
        String estadoLabel     = getEstadoLabel(estado);
        String colorEstado     = getColorEstado(estado);
        String bgEstado        = getBgEstado(estado);
        String fechaPedido     = pedido.getFechaPedido().format(dtf);
        String fechaEsperada   = pedido.getFechaEsperada() != null
                ? pedido.getFechaEsperada().format(df) : "No especificada";
        String notas           = nvl(pedido.getNotas(), null);
        String pedidoId        = String.valueOf(pedido.getId());

        // Bloque de precios
        String precioBloque = "";
        if (total != null) {
            precioBloque =
                "<tr>" +
                "  <td style='padding:12px 0;border-bottom:1px solid #f0f0f0'>" +
                "    <span style='color:#888;font-size:13px;font-weight:500;text-transform:uppercase;letter-spacing:.5px'>Precio unitario</span><br>" +
                "    <span style='color:#1a1a1a;font-size:16px;font-weight:600;margin-top:4px;display:block'>$" + String.format("%,.2f", precioUnit) + "</span>" +
                "  </td>" +
                "</tr>" +
                "<tr>" +
                "  <td style='padding:16px 0 4px'>" +
                "    <div style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:10px;padding:16px;text-align:center'>" +
                "      <span style='color:#15803d;font-size:13px;font-weight:600;text-transform:uppercase;letter-spacing:.5px;display:block;margin-bottom:4px'>Total del pedido</span>" +
                "      <span style='color:#15803d;font-size:28px;font-weight:800'>$" + String.format("%,.2f", total) + "</span>" +
                "    </div>" +
                "  </td>" +
                "</tr>";
        }

        // Bloque de notas
        String notasBloque = "";
        if (notas != null) {
            notasBloque =
                "<tr>" +
                "  <td style='padding-top:24px'>" +
                "    <div style='background:#fffbeb;border-left:4px solid #f59e0b;border-radius:0 8px 8px 0;padding:16px'>" +
                "      <p style='margin:0 0 6px;color:#92400e;font-size:12px;font-weight:700;text-transform:uppercase;letter-spacing:.5px'>Notas adicionales</p>" +
                "      <p style='margin:0;color:#78350f;font-size:14px;line-height:1.6'>" + notas + "</p>" +
                "    </div>" +
                "  </td>" +
                "</tr>";
        }

        return
            "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head>" +
            "  <meta charset='UTF-8'>" +
            "  <meta name='viewport' content='width=device-width,initial-scale=1'>" +
            "  <meta name='x-apple-disable-message-reformatting'>" +
            "  <title>Nuevo Pedido</title>" +
            "</head>" +
            "<body style='margin:0;padding:0;background-color:#f6f6f7;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica,Arial,sans-serif'>" +

            // Wrapper
            "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f6f6f7;padding:40px 16px'>" +
            "<tr><td align='center'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='max-width:560px'>" +

            // ── Logo / Marca ──────────────────────────────────────────────
            "<tr><td align='center' style='padding-bottom:24px'>" +
            "  <p style='margin:0;font-size:20px;font-weight:800;color:#1a1a1a;letter-spacing:-0.5px'>" + negocioNombre + "</p>" +
            "  <p style='margin:4px 0 0;font-size:13px;color:#888'>Sistema de Gestion de Pedidos</p>" +
            "</td></tr>" +

            // ── Card principal ───────────────────────────────────────────
            "<tr><td style='background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,.08),0 8px 24px rgba(0,0,0,.06)'>" +

            // Header del card (gradiente)
            "<table width='100%' cellpadding='0' cellspacing='0'>" +
            "<tr><td style='background:linear-gradient(135deg,#1e293b 0%,#334155 100%);padding:32px 32px 28px'>" +
            "  <table width='100%' cellpadding='0' cellspacing='0'>" +
            "  <tr>" +
            "    <td>" +
            "      <p style='margin:0 0 4px;color:rgba(255,255,255,.6);font-size:12px;font-weight:600;text-transform:uppercase;letter-spacing:1px'>Nuevo Pedido</p>" +
            "      <p style='margin:0;color:#ffffff;font-size:26px;font-weight:800;letter-spacing:-0.5px'>Pedido #" + pedidoId + "</p>" +
            "    </td>" +
            "    <td align='right' style='vertical-align:top'>" +
            "      <span style='display:inline-block;background:" + bgEstado + ";color:" + colorEstado + ";font-size:12px;font-weight:700;padding:6px 14px;border-radius:100px;letter-spacing:.3px'>" + estadoLabel + "</span>" +
            "    </td>" +
            "  </tr>" +
            "  </table>" +
            "  <p style='margin:16px 0 0;color:rgba(255,255,255,.5);font-size:13px'>" + fechaPedido + "</p>" +
            "</td></tr>" +
            "</table>" +

            // Cuerpo del card
            "<table width='100%' cellpadding='0' cellspacing='0'>" +
            "<tr><td style='padding:28px 32px'>" +

            // Datos del proveedor
            "<table width='100%' cellpadding='0' cellspacing='0'>" +
            "<tr><td style='padding-bottom:20px'>" +
            "  <p style='margin:0 0 12px;color:#888;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:1px'>Proveedor</p>" +
            "  <table width='100%' cellpadding='0' cellspacing='0' style='background:#f8fafc;border-radius:10px;overflow:hidden'>" +
            "  <tr><td style='padding:14px 16px;border-bottom:1px solid #f0f0f0'>" +
            "    <span style='color:#888;font-size:12px'>Empresa</span><br>" +
            "    <span style='color:#1a1a1a;font-size:15px;font-weight:600'>" + proveedorNombre + "</span>" +
            "  </td></tr>" +
            "  <tr><td style='padding:14px 16px;border-bottom:1px solid #f0f0f0'>" +
            "    <span style='color:#888;font-size:12px'>Contacto</span><br>" +
            "    <span style='color:#1a1a1a;font-size:14px;font-weight:500'>" + contacto + "</span>" +
            "  </td></tr>" +
            "  <tr><td style='padding:14px 16px'>" +
            "    <span style='color:#888;font-size:12px'>Telefono</span><br>" +
            "    <span style='color:#1a1a1a;font-size:14px;font-weight:500'>" + telefono + "</span>" +
            "  </td></tr>" +
            "  </table>" +
            "</td></tr>" +

            // Separador
            "<tr><td style='padding-bottom:20px'>" +
            "  <div style='height:1px;background:#f0f0f0'></div>" +
            "</td></tr>" +

            // Detalles del pedido
            "<tr><td>" +
            "  <p style='margin:0 0 12px;color:#888;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:1px'>Detalles del pedido</p>" +
            "  <table width='100%' cellpadding='0' cellspacing='0'>" +

            "  <tr><td style='padding:12px 0;border-bottom:1px solid #f0f0f0'>" +
            "    <span style='color:#888;font-size:13px;font-weight:500;text-transform:uppercase;letter-spacing:.5px'>Producto</span><br>" +
            "    <span style='color:#1a1a1a;font-size:16px;font-weight:600;margin-top:4px;display:block'>" + producto + "</span>" +
            "  </td></tr>" +

            "  <tr><td style='padding:12px 0;border-bottom:1px solid #f0f0f0'>" +
            "    <span style='color:#888;font-size:13px;font-weight:500;text-transform:uppercase;letter-spacing:.5px'>Descripcion</span><br>" +
            "    <span style='color:#374151;font-size:14px;line-height:1.5;margin-top:4px;display:block'>" + descripcion + "</span>" +
            "  </td></tr>" +

            "  <tr><td style='padding:12px 0;border-bottom:1px solid #f0f0f0'>" +
            "    <span style='color:#888;font-size:13px;font-weight:500;text-transform:uppercase;letter-spacing:.5px'>Cantidad</span><br>" +
            "    <span style='color:#1a1a1a;font-size:22px;font-weight:700;margin-top:4px;display:block'>" + cantidad + " <span style='font-size:14px;font-weight:500;color:#888'>unidades</span></span>" +
            "  </td></tr>" +

            precioBloque +

            "  <tr><td style='padding:12px 0;border-bottom:1px solid #f0f0f0'>" +
            "    <span style='color:#888;font-size:13px;font-weight:500;text-transform:uppercase;letter-spacing:.5px'>Fecha esperada</span><br>" +
            "    <span style='color:#1a1a1a;font-size:15px;font-weight:600;margin-top:4px;display:block'>" + fechaEsperada + "</span>" +
            "  </td></tr>" +

            notasBloque +

            "  </table>" +
            "</td></tr>" +

            "</table>" +

            "</td></tr>" +
            "</table>" +

            // Footer del card
            "<table width='100%' cellpadding='0' cellspacing='0'>" +
            "<tr><td style='background:#f8fafc;border-top:1px solid #f0f0f0;padding:20px 32px;text-align:center'>" +
            "  <p style='margin:0;color:#aaa;font-size:12px;line-height:1.6'>" +
            "    Este correo fue enviado automaticamente por el sistema de gestion de" +
            "    <strong style='color:#888'> " + negocioNombre + "</strong>.<br>" +
            "    Por favor no responder a este mensaje." +
            "  </p>" +
            "</td></tr>" +
            "</table>" +

            "</td></tr>" + // cierra card

            // Footer general
            "<tr><td align='center' style='padding-top:24px'>" +
            "  <p style='margin:0;color:#bbb;font-size:12px'>Powered by <strong style='color:#888'>" + negocioNombre + "</strong></p>" +
            "</td></tr>" +

            "</table>" +
            "</td></tr>" +
            "</table>" +

            "</body></html>";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private String getEstadoLabel(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "Pendiente";
            case "ENVIADO"   -> "Enviado";
            case "RECIBIDO"  -> "Recibido";
            case "CANCELADO" -> "Cancelado";
            default          -> estado;
        };
    }

    private String getColorEstado(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "#92400e";
            case "ENVIADO"   -> "#1e40af";
            case "RECIBIDO"  -> "#14532d";
            case "CANCELADO" -> "#7f1d1d";
            default          -> "#374151";
        };
    }

    private String getBgEstado(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "#fef3c7";
            case "ENVIADO"   -> "#dbeafe";
            case "RECIBIDO"  -> "#dcfce7";
            case "CANCELADO" -> "#fee2e2";
            default          -> "#f3f4f6";
        };
    }

    private String nvl(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
