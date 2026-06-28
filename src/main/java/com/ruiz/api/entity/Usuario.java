package com.ruiz.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String nombre;

    public enum Rol {
        ADMIN, EMPLEADO
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Rol rol = Rol.ADMIN; // Default

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // TODO: En Fase 4 se podría hacer N:M si un usuario administra varios negocios
    // Para simplificar ahora (MVP), un usuario pertenece a un Negocio principal.
    // Opcionalmente el Negocio tiene el dueño.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id")
    private Negocio negocio;
}
