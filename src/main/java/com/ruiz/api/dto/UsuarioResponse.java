package com.ruiz.api.dto;

import com.ruiz.api.entity.Usuario.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Long id;
    private String username;
    private String email;
    private String nombre;
    private Rol rol;
    private Boolean activo;
    private Long negocioId;
    private String negocioNombre;  // nombre del negocio asociado
}
