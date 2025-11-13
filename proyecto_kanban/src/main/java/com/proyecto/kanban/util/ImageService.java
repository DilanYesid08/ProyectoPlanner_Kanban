package com.proyecto.kanban.util;

import javafx.scene.image.Image;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilidad simple para cargar y cachear imágenes desde resources.
 * Uso: Image img = ImageService.load("/assets/logo.png");
 */
public final class ImageService {
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    private ImageService() {}

    /**
     * Carga una imagen desde el classpath y la cachea.
     * @param resourcePath ruta empezando por '/' relativa a src/main/resources
     * @return Image o null si no se encuentra
     */
    public static Image load(String resourcePath) {
        if (resourcePath == null) return null;
        return CACHE.computeIfAbsent(resourcePath, p -> {
            try {
                // load in background (true) to avoid blocking UI on large images
                return new Image(ImageService.class.getResource(p).toExternalForm(), true);
            } catch (Exception ex) {
                // Si falla, devuelve null y no cachea la excepción
                return null;
            }
        });
    }
}
