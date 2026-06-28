package com.ruiz.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateRequest {

    @Size(max = 100)
    private String nombre;

    @Email(message = "Formato de email inválido")
    @Size(max = 100)
    private String email;

    // Solo se actualiza si se manda — si es null se ignora
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
