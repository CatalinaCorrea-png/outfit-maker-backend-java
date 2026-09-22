package ar.outfitmaker.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutfitTest {

    private final User cher = new User("cher@gmail.com", "Cher", "", "hash");

    @Test
    void addTag_addingSameTagThrowsException() {
        Outfit outfit = new Outfit(cher);
        Tag casual = new Tag("casual", TagType.STYLE);
        outfit.addTag(casual);
        // deberia chequearse name de tag ???? chequear metodo de Outfit !
        assertThatThrownBy(() -> outfit.addTag(casual))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tag already exists");
    }

    @Test
    void aiGenerated_setAiGeneratedTrueAndSavesPrompt() {
        Outfit outfit = Outfit.aiGenerated(cher, "outfit for a casual dinner");

        assertThat(outfit.isAiGenerated()).isTrue();
        assertThat(outfit.getAiPrompt()).isEqualTo("outfit for a casual dinner");
    }

    @Test
    void manualOutfit_isNotAiGenerated() {
        Outfit outfit = new Outfit(cher);

        assertThat(outfit.isAiGenerated()).isFalse();
        assertThat(outfit.getAiPrompt()).isNull();
    }

}
