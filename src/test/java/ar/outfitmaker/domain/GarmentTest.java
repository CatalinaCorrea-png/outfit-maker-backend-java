package ar.outfitmaker.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.outfitmaker.errors.BusinessException;
import org.junit.jupiter.api.Test;

class GarmentTest {

    private final User cher = new User("cher@gmail.com", "Cher", "", "hash");
    private final Category shirt = new Category("Remera", Slot.UPPER);

    @Test
    void primaryImage_hasMinSortOrder_noMatterLoadingOrder() {
        Garment garment = Garment.builder(cher, shirt).name("Shirt").build();
        garment.addImage(new GarmentImage(garment, "second.jpg", 1));
        garment.addImage(new GarmentImage(garment, "first.jpg", 0));
        garment.addImage(new GarmentImage(garment, "third.jpg", 2));

        assertThat(garment.primaryImage())
                .map(GarmentImage::getImageUrl)
                .contains("first.jpg");
    }

    @Test
    void primaryImage_isEmptyWhenTheresNoImages() {
        Garment garment = Garment.builder(cher, shirt).name("Shirt").build();

        assertThat(garment.primaryImage()).isEmpty();
    }

    @Test
    void builder_keepsDefaultValues() {
        Garment garment = Garment.builder(cher, shirt).name("Shirt").build();

        assertThat(garment.isActive()).isTrue();
        assertThat(garment.getPattern()).isEqualTo(Pattern.SOLID);
        assertThat(garment.getSeason()).isEqualTo(Season.ALL_SEASONS);
    }

    @Test
    void validate_formalityOutOfBounds_throwsBusinessException() {
        // formality default = 0 - not initialized
        Garment garment = Garment.builder(cher, shirt).name("Shirt").build();

        assertThatThrownBy(() -> garment.validate())
                .isInstanceOf(BusinessException.class);
    }

    // TODO() More validate() tests...

}
