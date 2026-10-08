package ar.outfitmaker.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;

/**
 * Guarda las fotos y devuelve la clave (nombre) pública con la que se van a mostrar.
 * Hoy la implementa el disco local; pasar a un bucket (R2, B2) es otra implementación.
 */
public interface PhotoStorage {

    /** Guarda la foto (validada con PhotoFiles) y devuelve su clave en el storage. */
    String save(MultipartFile file);

    /** Borra la foto de esa clave. Si no es una clave de este storage, no hace nada. */
    void delete(String key);

    /** Las claves de las fotos guardadas hace más de maxAge. Para el barrido de huérfanos. */
    List<String> findKeysOlderThan(Duration maxAge);

    /**
     * La URL pública de una clave. Si ya es una URL absoluta (imágenes externas, como
     * las del seed), la devuelve tal cual. null si la clave es null.
     */
    String urlOf(String key);
}
