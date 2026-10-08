package ar.outfitmaker.service.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.outfitmaker.config.StorageProperties;
import ar.outfitmaker.errors.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;

class LocalPhotoStorageTest {

    private static final String BASE_URL = "http://localhost/uploads";

    @TempDir
    private Path dir;

    private LocalPhotoStorage storage;

    @BeforeEach
    void setup() {
        storage = new LocalPhotoStorage(new StorageProperties(dir.toString(), BASE_URL));
    }

    // ─── guardar ────────────────────────────────────────────────────────────

    @Test
    void save_storesTheFileAndReturnsItsKey() throws Exception {
        byte[] content = {1, 2, 3};

        String key = storage.save(new MockMultipartFile("original", "foto.webp", "image/webp", content));

        // Una clave es un nombre de archivo pelado: ni dominio ni carpeta
        assertThat(key).endsWith(".webp").doesNotContain("/");
        assertThat(Files.readAllBytes(dir.resolve(key))).isEqualTo(content);
    }

    @Test
    void save_neverReusesTheOriginalFileName() {
        String key = storage.save(new MockMultipartFile("original", "remera.jpg", "image/jpeg", new byte[]{1}));

        // UUID: el nombre que eligió el usuario no aparece en la clave (ni en la URL pública)
        assertThat(key).doesNotContain("remera");
    }

    @Test
    void save_rejectsUnsupportedTypes() {
        MockMultipartFile pdf = new MockMultipartFile("original", "doc.pdf", "application/pdf", new byte[]{1});

        assertThatThrownBy(() -> storage.save(pdf))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("PHOTO_INVALID_TYPE"));
    }

    @Test
    void save_rejectsEmptyFiles() {
        MockMultipartFile empty = new MockMultipartFile("original", "vacia.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> storage.save(empty))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("PHOTO_EMPTY"));
    }

    // ─── URL pública ────────────────────────────────────────────────────────

    @Test
    void urlOf_buildsThePublicUrlOfAKey() {
        assertThat(storage.urlOf("3f2a.webp")).isEqualTo(BASE_URL + "/3f2a.webp");
    }

    @Test
    void urlOf_leavesAbsoluteUrlsAsTheyAre() {
        // Las fotos del seed son de picsum: no están en este storage
        String picsum = "https://picsum.photos/seed/remera/600/750";

        assertThat(storage.urlOf(picsum)).isEqualTo(picsum);
        assertThat(storage.urlOf("http://otro-lado/foto.webp")).isEqualTo("http://otro-lado/foto.webp");
        assertThat(storage.urlOf(null)).isNull();
    }

    // ─── borrar ─────────────────────────────────────────────────────────────

    @Test
    void delete_removesTheFileOfThatKey() {
        String key = storage.save(new MockMultipartFile("original", "a.png", "image/png", new byte[]{1}));

        storage.delete(key);

        assertThat(dir.resolve(key)).doesNotExist();
    }

    @Test
    void delete_ignoresExternalUrlsAndPathTricks() throws Exception {
        Path outside = Files.writeString(dir.getParent().resolve("fuera-" + System.nanoTime() + ".txt"), "no tocar");
        try {
            // Las fotos del seed (picsum) no son de este storage
            storage.delete("https://picsum.photos/seed/remera/600/750");
            // Nada de escaparse de la carpeta con ../
            storage.delete("../" + outside.getFileName());
            storage.delete(null);

            assertThat(outside).exists();
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    // ─── listar las viejas (para el barrido) ────────────────────────────────

    @Test
    void findKeysOlderThan_returnsOnlyTheOldOnes() throws Exception {
        String oldKey = storage.save(new MockMultipartFile("original", "a.png", "image/png", new byte[]{1}));
        String newKey = storage.save(new MockMultipartFile("original", "b.png", "image/png", new byte[]{1}));
        Files.setLastModifiedTime(dir.resolve(oldKey), FileTime.from(Instant.now().minus(Duration.ofHours(2))));

        assertThat(storage.findKeysOlderThan(Duration.ofHours(1))).containsExactly(oldKey).doesNotContain(newKey);
    }
}
