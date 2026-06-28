package com.ruiz.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Sirve los archivos subidos como recursos estáticos.
     *
     * Una imagen guardada en: uploads/productos/abc123.jpg
     * será accesible en:      http://localhost:8080/uploads/productos/abc123.jpg
     *
     * El frontend puede usar esa URL directamente en un <img src="...">
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ruta absoluta de la carpeta uploads en el sistema de archivos
        String rutaAbsoluta = Paths.get(uploadDir).toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(rutaAbsoluta);
    }
}
