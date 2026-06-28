package com.ruiz.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtributoProductoDTO {

    private Long id;

    @NotBlank(message = "La clave del atributo es obligatoria")
    @Size(max = 100, message = "La clave no puede exceder los 100 caracteres")
    private String clave;       // Ej: "Color"

    @NotBlank(message = "El valor del atributo es obligatorio")
    @Size(max = 255, message = "El valor no puede exceder los 255 caracteres")
    private String valor;       // Ej: "Rojo"

    private Long productoId;
}
