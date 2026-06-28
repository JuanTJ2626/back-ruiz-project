package com.ruiz.api.service;

import org.springframework.web.multipart.MultipartFile;

public interface ArchivoService {

    /**
     * Guarda el archivo en el servidor y devuelve la URL pública para accederlo.
     * Ej: http://localhost:8080/uploads/productos/imagen-abc123.jpg
     */
    String guardarImagen(MultipartFile archivo, String subcarpeta);

    /** Elimina un archivo dado su nombre. */
    void eliminarImagen(String nombreArchivo, String subcarpeta);
}
