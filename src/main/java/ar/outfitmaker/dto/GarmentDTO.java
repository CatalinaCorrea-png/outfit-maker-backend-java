package ar.outfitmaker.dto;

import ar.outfitmaker.domain.*;

import java.util.function.Function;

public record GarmentDTO(
        String id,
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
        // Las dos caras para la card: adelante fija, atrás en hover.
        // Miniatura si hay, si no la original; null si la prenda no tiene esa foto.
        String frontImageUrl,
        String backImageUrl
) {
    public static GarmentDTO from(Garment garment, Function<String, String> photoUrl) {
        return new GarmentDTO(
                garment.getId(),
                garment.getCategory().getName(),
                garment.getName(),
                garment.getBrand(),
                garment.getPrimaryColor(),
                garment.getSecondaryColor(),
                garment.getPattern(),
                garment.getMaterial(),
                garment.getFormality(),
                garment.getFit(),
                garment.getSeason(),
                garment.getCareNotes(),
                garment.isActive(),
                garment.getCreatedAt().toString(),
                garment.getFront()
                        .map(GarmentPhoto::gridKey)
                        .map(photoUrl)
                        .orElse(null),
                garment.getBack()
                        .map(GarmentPhoto::gridKey)
                        .map(photoUrl)
                        .orElse(null)
        );
    }
}
