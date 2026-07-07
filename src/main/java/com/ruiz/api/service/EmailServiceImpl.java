package com.ruiz.api.service;

import com.ruiz.api.entity.PedidoProveedor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Override
    public void enviarNotificacionPedido(PedidoProveedor pedido) {
        try {
            // Validar que el proveedor tenga email
            if (pedido.getProveedor().getEmail() == null || pedido.getProveedor().getEmail().isBlank()) {
                log.warn("El proveedor {} no tiene email configurado", pedido.getProveedor().getNombre());
                throw new IllegalArgumentException("El proveedor no tiene email configurado");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(pedido.getProveedor().getEmail());
            helper.setSubject("🛒 Nuevo Pedido - " + pedido.getProveedor().getNegocio().getNombre());
            helper.setText(construirEmailHTML(pedido), true);

            mailSender.send(message);
            
            log.info("Email enviado exitosamente al proveedor: {} ({})", 
                    pedido.getProveedor().getNombre(), 
                    pedido.getProveedor().getEmail());

        } catch (MessagingException e) {
            log.error("Error al enviar email al proveedor {}: {}", 
                    pedido.getProveedor().getNombre(), 
                    e.getMessage());
            throw new RuntimeException("Error al enviar el email: " + e.getMessage(), e);
        }
    }

    /**
     * Construye el HTML del email con un diseño profesional.
     */
    private String construirEmailHTML(PedidoProveedor pedido) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String negocioNombre = pedido.getProveedor().getNegocio().getNombre();
        String proveedorNombre = pedido.getProveedor().getNombre();
        String contacto = pedido.getProveedor().getContacto() != null ? pedido.getProveedor().getContacto() : "N/A";
        String telefono = pedido.getProveedor().getTelefono() != null ? pedido.getProveedor().getTelefono() : "N/A";
        
        String descripcion = pedido.getDescripcion();
        Integer cantidad = pedido.getCantidad();
        Double precioUnitario = pedido.getPrecioUnitario();
        Double total = precioUnitario != null ? precioUnitario * cantidad : null;
        String estado = pedido.getEstado().toString();
        String fechaPedido = pedido.getFechaPedido().format(dateTimeFormatter);
        String fechaEsperada = pedido.getFechaEsperada() != null ? pedido.getFechaEsperada().format(dateFormatter) : "No especificada";
        String notas = pedido.getNotas() != null && !pedido.getNotas().isBlank() ? pedido.getNotas() : "Sin notas adicionales";
        String producto = pedido.getProducto() != null ? pedido.getProducto().getNombre() : "No especificado";

        return """
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Notificación de Pedido</title>
</head>
<body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
    <table role="presentation" style="width: 100%%; border-collapse: collapse;">
        <tr>
            <td align="center" style="padding: 40px 0;">
                <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: #ffffff; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                    
                    <!-- Header -->
                    <tr>
                        <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: bold;">
                                🛒 Nuevo Pedido
                            </h1>
                            <p style="margin: 10px 0 0 0; color: #ffffff; font-size: 16px; opacity: 0.9;">
                                %s
                            </p>
                        </td>
                    </tr>

                    <!-- Información del Proveedor -->
                    <tr>
                        <td style="padding: 30px; background-color: #f8f9fa;">
                            <h2 style="margin: 0 0 15px 0; color: #333; font-size: 18px; border-bottom: 2px solid #667eea; padding-bottom: 8px;">
                                📋 Información del Proveedor
                            </h2>
                            <table style="width: 100%%; border-collapse: collapse;">
                                <tr>
                                    <td style="padding: 8px 0; color: #666; width: 40%%;">
                                        <strong>Proveedor:</strong>
                                    </td>
                                    <td style="padding: 8px 0; color: #333;">
                                        %s
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 0; color: #666;">
                                        <strong>Contacto:</strong>
                                    </td>
                                    <td style="padding: 8px 0; color: #333;">
                                        %s
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 0; color: #666;">
                                        <strong>Teléfono:</strong>
                                    </td>
                                    <td style="padding: 8px 0; color: #333;">
                                        %s
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <!-- Detalles del Pedido -->
                    <tr>
                        <td style="padding: 30px;">
                            <h2 style="margin: 0 0 15px 0; color: #333; font-size: 18px; border-bottom: 2px solid #667eea; padding-bottom: 8px;">
                                📦 Detalles del Pedido
                            </h2>
                            <table style="width: 100%%; border-collapse: collapse; background-color: #f8f9fa; border-radius: 8px;">
                                <tr>
                                    <td style="padding: 15px; border-bottom: 1px solid #dee2e6;">
                                        <strong style="color: #666;">Producto:</strong>
                                        <div style="color: #333; margin-top: 5px;">%s</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 15px; border-bottom: 1px solid #dee2e6;">
                                        <strong style="color: #666;">Descripción:</strong>
                                        <div style="color: #333; margin-top: 5px;">%s</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 15px; border-bottom: 1px solid #dee2e6;">
                                        <strong style="color: #666;">Cantidad:</strong>
                                        <div style="color: #333; margin-top: 5px; font-size: 18px; font-weight: bold;">%d unidades</div>
                                    </td>
                                </tr>
                                %s
                                <tr>
                                    <td style="padding: 15px; border-bottom: 1px solid #dee2e6;">
                                        <strong style="color: #666;">Estado:</strong>
                                        <div style="margin-top: 5px;">
                                            <span style="background-color: %s; color: white; padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; text-transform: uppercase;">
                                                %s
                                            </span>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 15px; border-bottom: 1px solid #dee2e6;">
                                        <strong style="color: #666;">Fecha del Pedido:</strong>
                                        <div style="color: #333; margin-top: 5px;">%s</div>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="padding: 15px;">
                                        <strong style="color: #666;">Fecha Esperada:</strong>
                                        <div style="color: #333; margin-top: 5px;">%s</div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <!-- Notas Adicionales -->
                    <tr>
                        <td style="padding: 30px; background-color: #fff8e1;">
                            <h3 style="margin: 0 0 10px 0; color: #f57c00; font-size: 16px;">
                                💬 Notas Adicionales
                            </h3>
                            <p style="margin: 0; color: #666; line-height: 1.6;">
                                %s
                            </p>
                        </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                        <td style="padding: 30px; text-align: center; background-color: #f8f9fa; border-top: 1px solid #dee2e6;">
                            <p style="margin: 0 0 10px 0; color: #666; font-size: 14px;">
                                Este email fue generado automáticamente por el sistema de gestión de <strong>%s</strong>
                            </p>
                            <p style="margin: 0; color: #999; font-size: 12px;">
                                Por favor, no responder a este correo electrónico.
                            </p>
                        </td>
                    </tr>

                </table>
            </td>
        </tr>
    </table>
</body>
</html>
""".formatted(
            negocioNombre,
            proveedorNombre,
            contacto,
            telefono,
            producto,
            descripcion,
            cantidad,
            total != null ? 
                "<tr><td style=\"padding: 15px; border-bottom: 1px solid #dee2e6;\">" +
                "<strong style=\"color: #666;\">Precio Unitario:</strong>" +
                "<div style=\"color: #333; margin-top: 5px;\">$" + String.format("%.2f", precioUnitario) + "</div>" +
                "</td></tr>" +
                "<tr><td style=\"padding: 15px; border-bottom: 1px solid #dee2e6;\">" +
                "<strong style=\"color: #666;\">Total:</strong>" +
                "<div style=\"color: #28a745; margin-top: 5px; font-size: 20px; font-weight: bold;\">$" + String.format("%.2f", total) + "</div>" +
                "</td></tr>" : "",
            getColorEstado(estado),
            estado,
            fechaPedido,
            fechaEsperada,
            notas,
            negocioNombre
        );
    }

    /**
     * Retorna el color del badge según el estado del pedido.
     */
    private String getColorEstado(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "#ffc107";
            case "ENVIADO" -> "#17a2b8";
            case "RECIBIDO" -> "#28a745";
            case "CANCELADO" -> "#dc3545";
            default -> "#6c757d";
        };
    }
}
