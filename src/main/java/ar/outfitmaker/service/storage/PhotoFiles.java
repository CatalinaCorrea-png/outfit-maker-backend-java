package ar.outfitmaker.service.storage;

import ar.outfitmaker.errors.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Reglas sobre el archivo de una foto, independientes de dónde se guarde.
 * Separadas del storage para poder validar TODAS las fotos antes de escribir la primera:
 * si la tercera es un PDF, no tiene que haber quedado escrita ninguna.
 */
public final class PhotoFiles {

    // La extensión sale del tipo declarado: así el servidor la sirve siempre como imagen
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private PhotoFiles() {}

    /** Tira BusinessException si el archivo no sirve como foto. Ignora null (foto opcional que no vino). */
    public static void validate(MultipartFile file) {
        if (file != null) extensionOf(file);
    }

    /** La extensión con la que se guarda el archivo. Valida lo mismo que validate(). */
    public static String extensionOf(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("PHOTO_EMPTY", "La foto está vacía");

        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) throw new BusinessException("PHOTO_INVALID_TYPE", "La foto tiene que ser JPEG, PNG o WebP");
        return extension;
    }
}
