package ar.outfitmaker.controller;

import ar.outfitmaker.dto.GarmentCreateDTO;
import ar.outfitmaker.dto.GarmentDTO;
import ar.outfitmaker.dto.GarmentFilters;
import ar.outfitmaker.dto.GarmentPhotoFiles;
import ar.outfitmaker.dto.PageResponse;
import ar.outfitmaker.service.GarmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/garments")
public class GarmentController {

    private final GarmentService garmentService;

    public GarmentController(GarmentService garmentService) {
        this.garmentService = garmentService;
    }

    // get filtered garments or ALL when there are no filters
    @GetMapping("/filtered-garments")
    public PageResponse<GarmentDTO> getFilteredGarments(
            @ModelAttribute GarmentFilters garmentFilters,
            Authentication authentication
    ) {
        return garmentService.getGarments(garmentFilters, authentication.getName(), garmentFilters.toPageable());
    }

    // Alta atómica: los datos y las fotos llegan juntos en un solo multipart.
    // "garment" es el JSON de la prenda (con Content-Type application/json) y el resto, archivos.
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GarmentDTO> createGarment(
            @Valid @RequestPart("garment") GarmentCreateDTO dto,
            @RequestPart("front") MultipartFile front,
            @RequestPart(value = "frontThumb", required = false) MultipartFile frontThumb,
            @RequestPart(value = "back", required = false) MultipartFile back,
            @RequestPart(value = "backThumb", required = false) MultipartFile backThumb,
            Authentication authentication
    ) {
        GarmentPhotoFiles photos = new GarmentPhotoFiles(front, frontThumb, back, backThumb);
        GarmentDTO created = garmentService.createGarment(dto, photos, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
