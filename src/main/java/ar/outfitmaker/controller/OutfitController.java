package ar.outfitmaker.controller;

import ar.outfitmaker.dto.OutfitDTO;
import ar.outfitmaker.dto.OutfitFilters;
import ar.outfitmaker.dto.PageResponse;
import ar.outfitmaker.service.OutfitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/outfits")
public class OutfitController {

    private final OutfitService outfitService;

    public OutfitController(OutfitService outfitService) {
        this.outfitService = outfitService;
    }

    // get filtered outfits or ALL when there are no filters
    @GetMapping("/filtered-outfits")
    public PageResponse<OutfitDTO> getFilteredOutfits(@ModelAttribute OutfitFilters outfitFilters) {
        return outfitService.getOutfits(outfitFilters, outfitFilters.toPageable());
    }
}
