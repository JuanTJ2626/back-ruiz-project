package com.ruiz.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ArchivoServiceImpl implements ArchivoService {

    // Extensiones permitidas
    private static final List<String> EXTENSIONES_PERMITIDAS = List.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    // Tamaño máximo: 5 MB
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;

    private final Cloudinary cloudinary;

    public ArchivoServiceImpl(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret,
                "secure",     true
        ));
    }

    @Override
    public String guardarImagen(MultipartFile archivo, String subcarpeta) {
        validarArchivo(archivo);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    archivo.getBytes(),
                    ObjectUtils.asMap(
                            "folder",          "ruiz/" + subcarpeta,
                            "resource_type",   "image",
                            "use_filename",    false,
                            "unique_filename", true
                    )
            );

            String url = (String) result.get("secure_url");
            log.info("Imagen subida a Cloudinary: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Error al subir imagen a Cloudinary: {}", e.getMessage());
            throw new RuntimeException("No se pudo subir la imagen", e);
        }
    }

    @Override
    public void eliminarImagen(String nombreArchivo, String subcarpeta) {
        // El publicId en Cloudinary es: ruiz/<subcarpeta>/<nombreArchivo sin extensión>
        String publicId = "ruiz/" + subcarpeta + "/" + nombreArchivo
                .replaceAll("\\.[^.]+$", ""); // quita extensión

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Imagen eliminada de Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.warn("No se pudo eliminar imagen de Cloudinary {}: {}", publicId, e.getMessage());
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
                "El archivo excede el tamaño máximo de 5 MB. " +
                "Recibido: " + (archivo.getSize() / 1024 / 1024) + " MB"
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
