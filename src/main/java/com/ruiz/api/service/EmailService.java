package com.ruiz.api.service;

import com.ruiz.api.entity.PedidoProveedor;

/**
 * Servicio para envío de emails.
 */
public interface EmailService {

    /**
     * Envía un email al proveedor con los detalles del pedido.
     * 
     * @param pedido El pedido a notificar
     */
    void enviarNotificacionPedido(PedidoProveedor pedido);
}
