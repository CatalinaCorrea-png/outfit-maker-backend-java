package ar.outfitmaker.dto;

import ar.outfitmaker.domain.Outfit;
import ar.outfitmaker.domain.OutfitItem;
import ar.outfitmaker.domain.Tag;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public record OutfitDTO(
        String id,
        String name,
        String notes,
        boolean aiGenerated,
        Integer rating,
        String createdAt,
        List<GarmentDTO> garments,
        List<String> tags
) {
    public static OutfitDTO from(Outfit outfit, Function<String, String> photoUrl) {
        return new OutfitDTO(
                outfit.getId(),
                outfit.getName(),
                outfit.getNotes(),
                outfit.isAiGenerated(),
                outfit.getRating(),
                outfit.getCreatedAt().toString(),
                outfit.getItems().stream()
                        .sorted(Comparator.comparingInt(OutfitItem::getLayerOrder))
                        .map(item -> GarmentDTO.from(item.getGarment(), photoUrl))
                        .toList(),
                outfit.getTags().stream()
                        .map(Tag::getName)
                        .toList()
        );
    }
}