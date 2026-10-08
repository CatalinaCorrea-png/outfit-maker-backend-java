package ar.outfitmaker.dto;

import ar.outfitmaker.domain.*;
import ar.outfitmaker.domain.Pattern;
import jakarta.validation.constraints.*;

public record GarmentCreateDTO(
        @NotBlank
        String category,   // category name

        @NotBlank
        @Size(max = Garment.MAX_TEXT_LENGTH)
        String name,

        @Size(max = Garment.MAX_TEXT_LENGTH)
        String brand,

        @NotBlank
        @jakarta.validation.constraints.Pattern(regexp = Garment.COLOR_REGEX)
        String primaryColor,

        // Opcional: el "?" hace opcional todo el grupo, así pasa "" (sin color secundario)
        @jakarta.validation.constraints.Pattern(regexp = Garment.COLOR_REGEX + "?")
        String secondaryColor,

        @NotNull
        Pattern pattern,

        @Size(max = Garment.MAX_TEXT_LENGTH)
        String material,

        @Min(1)
        @Max(5)
        int formality,

        @NotNull
        Fit fit,

        @NotNull
        Season season,

        @Size(max = Garment.MAX_TEXT_LENGTH)
        String careNotes

        // Las fotos no viajan acá: llegan como archivos en el mismo request (GarmentPhotoFiles)
) { }
