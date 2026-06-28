package com.ruiz.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ArchivoServiceImpl implements ArchivoService {

    // Carpeta raíz donde se guardan las imágenes (configurable en application.properties)
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // URL base del servidor (configurable por perfil)
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // Extensiones permitidas
    private static final List<String> EXTENSIONES_PERMITIDAS = List.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    // Tamaño máximo: 5 MB
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;

    @Override
    public String guardarImagen(MultipartFile archivo, String subcarpeta) {
        validarArchivo(archivo);

        try {
            // Crear carpeta si no existe: uploads/productos/ o uploads/negocios/
            Path dirPath = Paths.get(uploadDir, subcarpeta);
            Files.createDirectories(dirPath);

            // Nombre único para evitar colisiones
            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreUnico = UUID.randomUUID().toString() + "." + extension;

            // Guardar el archivo
            Path destino = dirPath.resolve(nombreUnico);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            // Devolver URL pública accesible desde el frontend
            // Ejemplo: http://localhost:8080/uploads/productos/abc123.jpg
            String urlPublica = baseUrl + "/uploads/" + subcarpeta + "/" + nombreUnico;
            log.info("Imagen guardada: {}", urlPublica);
            return urlPublica;

        } catch (IOException e) {
            log.error("Error al guardar imagen: {}", e.getMessage());
            throw new RuntimeException("No se pudo guardar la imagen en el servidor", e);
        }
    }

    @Override
    public void eliminarImagen(String nombreArchivo, String subcarpeta) {
        try {
            Path rutaArchivo = Paths.get(uploadDir, subcarpeta, nombreArchivo);
            if (Files.exists(rutaArchivo)) {
                Files.delete(rutaArchivo);
                log.info("Imagen eliminada: {}/{}", subcarpeta, nombreArchivo);
            }
        } catch (IOException e) {
            log.warn("No se pudo eliminar la imagen {}/{}: {}", subcarpeta, nombreArchivo, e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o no fue enviado");
        }

        if (archivo.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException(
                "El archivo excede el tamaño máximo permitido de 5 MB. " +
                "Tamaño recibido: " + (archivo.getSize() / 1024 / 1024) + " MB"
            );
        }

        String extension = obtenerExtension(archivo.getOriginalFilename());
        if (!EXTENSIONES_PERMITIDAS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException(
                "Formato no permitido: ." + extension +
                ". Formatos aceptados: " + String.join(", ", EXTENSIONES_PERMITIDAS)
            );
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            throw new IllegalArgumentException("El archivo no tiene extensión válida");
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1);
    }
}
