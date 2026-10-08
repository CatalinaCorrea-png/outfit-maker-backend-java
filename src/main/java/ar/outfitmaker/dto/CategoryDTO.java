package ar.outfitmaker.dto;

import ar.outfitmaker.domain.Category;
import ar.outfitmaker.domain.Slot;

public record CategoryDTO(
        String name,
        // La zona del cuerpo: el armado de outfits la va a usar para ubicar cada prenda
        Slot slot
) {
    public static CategoryDTO from(Category category) {
        return new CategoryDTO(category.getName(), category.getSlot());
    }
}
