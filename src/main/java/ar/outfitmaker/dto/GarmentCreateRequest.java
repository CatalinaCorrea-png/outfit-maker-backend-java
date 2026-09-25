package ar.outfitmaker.dto;

import ar.outfitmaker.domain.Fit;
import ar.outfitmaker.domain.Pattern;
import ar.outfitmaker.domain.Season;

public record GarmentCreateRequest(
        String id,
        String userId,
        String category,   // category name
        String name,
        String brand,
        String primaryColor,
        String secondaryColor,
        Pattern pattern,
        String material,
        int formality,
        Fit fit,
        Season season,
        String careNotes,
        boolean active,
        String createdAt,
        String imageUrl
) {
}
