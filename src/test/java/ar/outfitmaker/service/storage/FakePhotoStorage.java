package ar.outfitmaker.service.storage;

import ar.outfitmaker.errors.InternalException;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Storage en memoria para los tests: registra qué claves se guardaron y cuáles se borraron,
 * y puede simular que falla la escritura número N.
 */
public class FakePhotoStorage implements PhotoStorage {

    public static final String BASE_URL = "http://fake/uploads";

    public final List<String> saved = new ArrayList<>();
    public final List<String> deleted = new ArrayList<>();
    // Lo que devuelve findKeysOlderThan: las fotos "viejas" que el barrido va a revisar
    public final List<String> oldKeys = new ArrayList<>();

    private int saveCalls = 0;
    private int failOnSave = -1;

    /** La escritura número n (empezando en 1) tira error, como si se llenara el disco. */
    public void failOnSave(int n) {
        this.failOnSave = n;
    }

    public void reset() {
        saved.clear();
        deleted.clear();
        oldKeys.clear();
        saveCalls = 0;
        failOnSave = -1;
    }

    @Override
    public String save(MultipartFile file) {
        PhotoFiles.extensionOf(file);
        saveCalls++;
        if (saveCalls == failOnSave) throw new InternalException("PHOTO_STORAGE_FAILED", "falla simulada");

        String key = saveCalls + ".webp";
        saved.add(key);
        return key;
    }

    @Override
    public void delete(String key) {
        deleted.add(key);
    }

    @Override
    public List<String> findKeysOlderThan(Duration maxAge) {
        return List.copyOf(oldKeys);
    }

    // Misma regla que LocalPhotoStorage: las URLs absolutas pasan tal cual
    @Override
    public String urlOf(String key) {
        if (key == null) return null;
        if (key.startsWith("http://") || key.startsWith("https://")) return key;
        return BASE_URL + "/" + key;
    }
}
