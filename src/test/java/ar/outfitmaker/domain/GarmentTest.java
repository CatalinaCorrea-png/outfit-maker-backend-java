package ar.outfitmaker.domain;

import ar.outfitmaker.errors.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class GarmentTest {

    private final User cher = new User("cher@gmail.com", "Cher", "", "hash");
    private final Category shirt = new Category("Shirt", Slot.UPPER);

    @Test
    void primaryImage_returnsLowestSortOrder_regardlessOfInsertionOrder() {
        Garment garment = Garment.builder(cher, shirt).name("Shirt").build();
        garment.addImage(new GarmentImage(garment, "second.jpg", 1));
        garment.addImage(new GarmentImage(garment, "first.jpg", 0));
        garment.addImage(new GarmentImage(garment, "third.jpg", 2));

        assertThat(garment.primaryImage())
                .map(GarmentImage::getImageUrl)
                .contains("first.jpg");
    }

    @Test
    void primaryImage_isEmptyWithoutImages() {
        Garment garment = Garment.builder(cher, shirt).name("Shirt").build();

        assertThat(garment.primaryImage()).isEmpty();
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
        Garment garment = Garment.builder(cher, shirt).name("Shirt").formality(formality).build();

        assertThatThrownBy(garment::validate)
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("INVALID_FORMALITY_NUMBER"));
    }

    @Test
    void validate_formalityInBounds_doesNotThrow() {
        Garment garment = Garment.builder(cher, shirt).name("Shirt").formality(1).build();

        assertThatCode(garment::validate).doesNotThrowAnyException();
    }

    // TODO() More validate() tests...

}
