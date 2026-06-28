package com.ruiz.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Atributo personalizado de un producto.
 * Permite que cada negocio defina sus propios atributos según su giro.
 *
 * Ejemplos:
 *   Boutique  → clave: "Color",   valor: "Rojo"
 *   Boutique  → clave: "Talla",   valor: "M"
 *   Ferretería → clave: "Material", valor: "Acero inoxidable"
 *   Panadería  → clave: "Peso",    valor: "500g"
 */
@Entity
@Table(name = "atributos_producto",
       uniqueConstraints = @UniqueConstraint(columnNames = {"producto_id", "clave"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtributoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String clave;       // Ej: "Color", "Talla", "Material"

    @Column(nullable = false, length = 255)
    private String valor;       // Ej: "Rojo", "M", "Acero"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}
