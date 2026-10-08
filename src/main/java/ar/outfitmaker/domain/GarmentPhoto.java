package ar.outfitmaker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Una cara de la prenda (adelante o atrás) con sus versiones derivadas.
 * No es una entidad: no tiene tabla ni id, sus columnas viven en la fila de garments.
 * Hibernate la arma con el constructor canónico a partir de las tres columnas.
 *
 * Los nombres de columna son los de base: Garment les pone el prefijo por cada uso.
 * Sin nullable = false a propósito: adelante y atrás tienen reglas distintas.
 */
@Embeddable
public record GarmentPhoto(
        // Clave en el storage (ej: "3f2a….webp"). Las imágenes externas (el seed)
        // guardan la URL absoluta: PhotoStorage.urlOf las devuelve tal cual.
        // La original, tal como la subió el usuario
        @Column(name = "photo_key") String key,

        // Versión chica para la grilla (~400px de ancho)
        @Column(name = "thumb_key") String thumbKey,

        // Recorte sin fondo (WebP con alfa) para el collage
        @Column(name = "cutout_key") String cutoutKey
) {
    /** La que va en la card: la miniatura, o la original si todavía no hay miniatura. */
    public String gridKey() {
        return thumbKey != null ? thumbKey : key;
    }
}
