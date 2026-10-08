package ar.outfitmaker.domain;

import ar.outfitmaker.errors.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class GarmentTest {

    private final User cher = new User("cher@gmail.com", "Cher", "", "hash");
    private final Category shirt = new Category("Shirt", Slot.UPPER);

    // ─── fotos ──────────────────────────────────────────────────────────────

    @Test
    void photos_areEmptyWithoutPhotos() {
        Garment garment = Garment.builder(cher, shirt).name("Shirt").build();

        assertThat(garment.getFront()).isEmpty();
        assertThat(garment.getBack()).isEmpty();
    }

    @Test
    void photos_keepFrontAndBackApart() {
        Garment garment = Garment.builder(cher, shirt).name("Shirt")
                .front(new GarmentPhoto("front.jpg", null, null))
                .back(new GarmentPhoto("back.jpg", null, null))
                .build();

        assertThat(garment.getFront()).map(GarmentPhoto::key).contains("front.jpg");
        assertThat(garment.getBack()).map(GarmentPhoto::key).contains("back.jpg");
    }

    @Test
    void gridKey_usesTheThumbnailWhenThereIsOne() {
        GarmentPhoto photo = new GarmentPhoto("original.jpg", "thumb.jpg", null);

        assertThat(photo.gridKey()).isEqualTo("thumb.jpg");
    }

    @Test
    void gridKey_fallsBackToTheOriginalWithoutThumbnail() {
        GarmentPhoto photo = new GarmentPhoto("original.jpg", null, null);

        assertThat(photo.gridKey()).isEqualTo("original.jpg");
    }

    @Test
    void builder_appliesDefaultValues() {
        Garment garment = Garment.builder(cher, shirt).name("Shirt").build();

        assertThat(garment.isActive()).isTrue();
        assertThat(garment.getPattern()).isEqualTo(Pattern.SOLID);
        assertThat(garment.getSeason()).isEqualTo(Season.ALL_SEASONS);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 6})
    void validate_formalityOutOfBounds_throwsBusinessException(int formality) {
        Garment garment = validGarment().formality(formality).build();

        assertThatThrownBy(garment::validate)
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("INVALID_FORMALITY_NUMBER"));
    }

    @Test
    void validate_formalityInBounds_doesNotThrow() {
        Garment garment = validGarment().build();

        assertThatCode(garment::validate).doesNotThrowAnyException();
    }

    // TODO() More validate() tests...

    // ─── helpers ────────────────────────────────────────────────────────────

    /**
     * Una prenda que pasa todas las reglas de validate(). Cada test rompe una sola cosa
     * encima: así una regla nueva no hace fallar los tests de las otras.
     */
    private Garment.Builder validGarment() {
        return Garment.builder(cher, shirt)
                .name("Shirt")
                .primaryColor("navy")
                .formality(1);
    }

}
