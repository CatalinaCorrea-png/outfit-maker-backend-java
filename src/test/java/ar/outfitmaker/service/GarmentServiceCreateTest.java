package ar.outfitmaker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.outfitmaker.domain.Category;
import ar.outfitmaker.domain.Fit;
import ar.outfitmaker.domain.Garment;
import ar.outfitmaker.domain.Pattern;
import ar.outfitmaker.domain.Season;
import ar.outfitmaker.domain.Slot;
import ar.outfitmaker.domain.User;
import ar.outfitmaker.dto.GarmentCreateDTO;
import ar.outfitmaker.dto.GarmentDTO;
import ar.outfitmaker.dto.GarmentPhotoFiles;
import ar.outfitmaker.errors.AppException;
import ar.outfitmaker.repository.CategoryRepository;
import ar.outfitmaker.repository.GarmentRepository;
import ar.outfitmaker.repository.UserRepository;
import ar.outfitmaker.service.storage.FakePhotoStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * La atomicidad del alta: nunca una prenda sin fotos ni una foto sin prenda.
 *
 * NOT_SUPPORTED es clave: @DataJpaTest envuelve cada test en una transacción que hace
 * rollback al final, y eso borraría las fotos incluso cuando el alta sale bien. Así, la
 * única transacción es la del service, con su commit o rollback reales.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({GarmentService.class, GarmentServiceCreateTest.Storage.class})
class GarmentServiceCreateTest {

    @TestConfiguration
    static class Storage {
        @Bean
        FakePhotoStorage photoStorage() {
            return new FakePhotoStorage();
        }
    }

    @Autowired
    private GarmentService garmentService;

    @Autowired
    private FakePhotoStorage photoStorage;

    @Autowired
    private GarmentRepository garmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setup() {
        photoStorage.reset();
        userRepository.save(new User("cher@gmail.com", "Cher", "", "hash"));
        categoryRepository.save(new Category("Remera", Slot.UPPER));
    }

    // Sin la transacción del test, lo commiteado queda: se limpia a mano
    @AfterEach
    void cleanup() {
        garmentRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ─── éxito ──────────────────────────────────────────────────────────────

    @Test
    void create_storesThePhotosAndTheGarmentTogether() {
        GarmentDTO created = garmentService.createGarment(dto("Remera rayada", 2), allPhotos(), "cher@gmail.com");

        assertThat(photoStorage.saved).hasSize(4);
        assertThat(photoStorage.deleted).isEmpty();
        // En la base van claves; la card recibe la URL que arma el storage con la miniatura
        assertThat(created.frontImageUrl()).isEqualTo(photoStorage.urlOf(photoStorage.saved.get(1)));
        assertThat(created.backImageUrl()).isEqualTo(photoStorage.urlOf(photoStorage.saved.get(3)));

        Garment stored = onlyGarment();
        assertThat(stored.getFront()).hasValueSatisfying(photo -> {
            assertThat(photo.key()).isEqualTo(photoStorage.saved.get(0));
            assertThat(photo.thumbKey()).isEqualTo(photoStorage.saved.get(1));
        });
    }

    @Test
    void create_withOnlyTheFrontPhoto_leavesTheBackEmpty() {
        garmentService.createGarment(dto("Jean", 2), new GarmentPhotoFiles(image("front"), null, null, null), "cher@gmail.com");

        assertThat(photoStorage.saved).hasSize(1);
        assertThat(onlyGarment().getBack()).isEmpty();
    }

    // ─── lo que falla ANTES de escribir: no se toca el disco ────────────────

    @Test
    void create_withAnInvalidBackPhoto_writesNothing() {
        GarmentPhotoFiles photos = new GarmentPhotoFiles(image("front"), image("frontThumb"),
                new MockMultipartFile("back", "doc.pdf", "application/pdf", new byte[]{1}), null);

        assertCode(() -> garmentService.createGarment(dto("Remera", 2), photos, "cher@gmail.com"), "PHOTO_INVALID_TYPE");

        // Validó la cuarta antes de escribir la primera
        assertThat(photoStorage.saved).isEmpty();
        assertThat(garmentRepository.count()).isZero();
    }

    @Test
    void create_withARepeatedName_writesNothing() {
        garmentService.createGarment(dto("Remera rayada", 2), allPhotos(), "cher@gmail.com");
        photoStorage.reset();

        assertCode(() -> garmentService.createGarment(dto("Remera rayada", 2), allPhotos(), "cher@gmail.com"),
                "GARMENT_ALREADY_EXISTS");

        assertThat(photoStorage.saved).isEmpty();
        assertThat(garmentRepository.count()).isOne();
    }

    // ─── lo que falla DESPUÉS de escribir: el rollback borra lo escrito ─────

    @Test
    void create_whenWritingAPhotoFails_deletesThePhotosAlreadyWritten() {
        photoStorage.failOnSave(3); // falla la foto de atrás: la de adelante y su miniatura ya están

        assertCode(() -> garmentService.createGarment(dto("Remera", 2), allPhotos(), "cher@gmail.com"),
                "PHOTO_STORAGE_FAILED");

        assertThat(photoStorage.saved).hasSize(2);
        assertThat(photoStorage.deleted).containsExactlyInAnyOrderElementsOf(photoStorage.saved);
        assertThat(garmentRepository.count()).isZero();
    }

    @Test
    void create_whenDomainValidationFails_deletesThePhotos() {
        // Formalidad fuera de rango: el controller lo frenaría con @Valid, pero el
        // service no puede asumir que todos sus llamadores pasan por ahí
        assertCode(() -> garmentService.createGarment(dto("Remera", 9), allPhotos(), "cher@gmail.com"),
                "INVALID_FORMALITY_NUMBER");

        assertThat(photoStorage.saved).hasSize(4);
        assertThat(photoStorage.deleted).containsExactlyInAnyOrderElementsOf(photoStorage.saved);
        assertThat(garmentRepository.count()).isZero();
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private static GarmentCreateDTO dto(String name, int formality) {
        return new GarmentCreateDTO("Remera", name, "Zara", "#ffffff", "", Pattern.SOLID,
                "algodón", formality, Fit.REGULAR, Season.SUMMER, null);
    }

    private static MockMultipartFile image(String part) {
        return new MockMultipartFile(part, part + ".webp", "image/webp", new byte[]{1, 2, 3});
    }

    private static GarmentPhotoFiles allPhotos() {
        return new GarmentPhotoFiles(image("front"), image("frontThumb"), image("back"), image("backThumb"));
    }

    private Garment onlyGarment() {
        assertThat(garmentRepository.count()).isOne();
        return garmentRepository.findAll().iterator().next();
    }

    private static void assertCode(Runnable call, String code) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(AppException.class, ex -> assertThat(ex.getCode()).isEqualTo(code));
    }
}
