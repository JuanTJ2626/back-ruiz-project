package com.ruiz.api.controller;

import com.ruiz.api.service.ArchivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "Subida de Imágenes", description = "Endpoints para subir imágenes de productos y negocios")
public class ArchivoController {

    private final ArchivoService archivoService;

    @PostMapping(value = "/producto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Subir imagen de producto",
        description = "Sube una imagen (JPG, PNG, GIF, WEBP — máx. 5MB) y devuelve la URL pública. " +
                      "Guarda esa URL en el campo imagenUrl al crear/actualizar el producto."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Imagen subida correctamente"),
        @ApiResponse(responseCode = "400", description = "Archivo inválido, muy grande o formato no permitido")
    })
    public ResponseEntity<Map<String, String>> subirImagenProducto(
            @Parameter(description = "Archivo de imagen (jpg, png, gif, webp — máx. 5MB)")
            @RequestParam("archivo") MultipartFile archivo) {

        String url = archivoService.guardarImagen(archivo, "productos");
        return ResponseEntity.ok(Map.of(
            "url", url,
            "mensaje", "Imagen subida correctamente"
        ));
    }

    @PostMapping(value = "/negocio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Subir logo de negocio",
        description = "Sube el logo del negocio y devuelve la URL pública."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logo subido correctamente"),
        @ApiResponse(responseCode = "400", description = "Archivo inválido")
    })
    public ResponseEntity<Map<String, String>> subirLogoNegocio(
            @Parameter(description = "Archivo de imagen del logo")
            @RequestParam("archivo") MultipartFile archivo) {

        String url = archivoService.guardarImagen(archivo, "negocios");
        return ResponseEntity.ok(Map.of(
            "url", url,
            "mensaje", "Logo subido correctamente"
        ));
    }

    @DeleteMapping("/producto/{nombreArchivo}")
    @Operation(summary = "Eliminar imagen de producto")
    @ApiResponse(responseCode = "200", description = "Imagen eliminada")
    public ResponseEntity<Map<String, String>> eliminarImagenProducto(
            @Parameter(description = "Nombre del archivo a eliminar (solo el nombre, no la URL completa)")
            @PathVariable String nombreArchivo) {

        archivoService.eliminarImagen(nombreArchivo, "productos");
        return ResponseEntity.ok(Map.of("mensaje", "Imagen eliminada correctamente"));
    }
}
