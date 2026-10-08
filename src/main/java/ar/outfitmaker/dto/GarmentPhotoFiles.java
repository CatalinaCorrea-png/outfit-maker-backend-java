package ar.outfitmaker.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Los archivos de las dos caras que llegan con el alta de una prenda.
 * La miniatura la genera el navegador; sin ella la card usa la original.
 *
 * @param front      obligatoria
 * @param frontThumb opcional
 * @param back       opcional
 * @param backThumb  opcional, se ignora si no vino la de atrás
 */
public record GarmentPhotoFiles(
        MultipartFile front,
        MultipartFile frontThumb,
        MultipartFile back,
        MultipartFile backThumb
) {
    /** Los archivos que de verdad se van a guardar, para validarlos todos antes de escribir. */
    public List<MultipartFile> toStore() {
        return Arrays.asList(front, frontThumb, back, back == null ? null : backThumb).stream()
                .filter(Objects::nonNull)
                .toList();
    }
}
