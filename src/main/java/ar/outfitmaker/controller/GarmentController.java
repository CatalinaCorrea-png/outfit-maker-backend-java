package ar.outfitmaker.controller;

import ar.outfitmaker.dto.GarmentCreateRequest;
import ar.outfitmaker.dto.GarmentDTO;
import ar.outfitmaker.dto.GarmentFilters;
import ar.outfitmaker.dto.PageResponse;
import ar.outfitmaker.service.GarmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/garments")
public class GarmentController {

    private final GarmentService garmentService;

    public GarmentController(GarmentService garmentService) {
        this.garmentService = garmentService;
    }

    // get filtered garments or ALL when there are no filters
    @GetMapping("/filtered-garments")
    public PageResponse<GarmentDTO> getFilteredGarments(@ModelAttribute GarmentFilters garmentFilters) {
        return garmentService.getGarments(garmentFilters, garmentFilters.toPageable());
    }

    @PostMapping("/create-garment")
    public GarmentDTO createGarment(
            @RequestBody GarmentCreateRequest garmentCreateRequest
    ) {
        return garmentService.createGarment(garmentCreateRequest);
    }
}
