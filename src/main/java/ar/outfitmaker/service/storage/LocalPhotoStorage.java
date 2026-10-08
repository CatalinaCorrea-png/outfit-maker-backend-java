package ar.outfitmaker.service.storage;

import ar.outfitmaker.config.StorageProperties;
import ar.outfitmaker.errors.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Guarda las fotos en una carpeta del disco, servida en /uploads/** (ver StorageConfiguration).
 * Los nombres son UUID: la ruta es pública porque los <img> no mandan el token,
 * así que la URL no tiene que poder adivinarse.
 */
@Service
public class LocalPhotoStorage implements PhotoStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalPhotoStorage.class);

    private final Path dir;
    private final String publicBaseUrl;

    public LocalPhotoStorage(StorageProperties storageProperties) {
        this.dir = Path.of(storageProperties.dir()).toAbsolutePath();
        this.publicBaseUrl = storageProperties.publicBaseUrl();
        try {
            Files.createDirectories(dir);
        } catch (IOException ex) {
            throw new InternalException("PHOTO_STORAGE_FAILED", "No se pudo crear la carpeta de fotos: " + dir);
        }
    }

    @Override
    public String save(MultipartFile file) {
        String fileName = UUID.randomUUID() + "." + PhotoFiles.extensionOf(file);
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, dir.resolve(fileName));
        } catch (IOException ex) {
            throw new InternalException("PHOTO_STORAGE_FAILED", "No se pudo guardar la foto: " + ex.getMessage());
        }
        return fileName;
    }

    @Override
    public void delete(String key) {
        if (!isPlainFileName(key)) return;   // URLs externas (tienen "/") o intentos de "../"
        try {
            Files.deleteIfExists(dir.resolve(key));
        } catch (IOException ex) {
            // Se llama limpiando (rollback, barrido): un fallo se loguea, no corta nada.
            // Si el archivo quedó, el próximo barrido lo vuelve a intentar.
            log.warn("No se pudo borrar la foto {}", key, ex);
        }
    }

    private static boolean isPlainFileName(String key) {
        return key != null && !key.isEmpty()
                && !key.contains("/") && !key.contains("\\") && !key.contains("..");
    }

    @Override
    public List<String> findKeysOlderThan(Duration maxAge) {
        Instant limit = Instant.now().minus(maxAge);
        try (Stream<Path> files = Files.list(dir)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(file -> lastModified(file).isBefore(limit))
                    .map(file -> file.getFileName().toString())
                    .toList();
        } catch (IOException ex) {
            log.warn("No se pudo listar la carpeta de fotos {}", dir, ex);
            return List.of();
        }
    }

    @Override
    public String urlOf(String key) {
        if (key == null) return null;
        if (key.startsWith("http://") || key.startsWith("https://")) return key;
        return publicBaseUrl + "/" + key;
    }

    private static Instant lastModified(Path file) {
        try {
            return Files.getLastModifiedTime(file).toInstant();
        } catch (IOException ex) {
            // Si no se puede leer la fecha, se lo trata como nuevo: mejor no borrarlo
            return Instant.now();
        }
    }
}
