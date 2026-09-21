package ar.outfitmaker.dto;

import ar.outfitmaker.domain.*;

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
        String imageUrl
) {
    public static GarmentDTO from(Garment garment) {
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
                garment.primaryImage()
                        .map(GarmentImage::getImageUrl)
                        .orElse(null)
        );
    }
}
