package com.ruiz.api.dto;

import com.ruiz.api.entity.Usuario.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    @Email(message = "Formato de email inválido")
    private String email;

    private String nombre;

    // Opcional — si se manda, el ADMIN puede crear empleados directamente
    // Si es null, el register público siempre crea ADMIN
    private Rol rol;

    // Opcional — si se manda, el empleado queda asociado a ese negocio
    // Si es null, se crea un negocio propio (flujo normal de registro público)
    private Long negocioId;
}
