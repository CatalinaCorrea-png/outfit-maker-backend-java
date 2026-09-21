package ar.outfitmaker.service;

import ar.outfitmaker.domain.Garment;
import ar.outfitmaker.dto.GarmentDTO;
import ar.outfitmaker.dto.GarmentFilters;
import ar.outfitmaker.dto.PageResponse;
import ar.outfitmaker.repository.GarmentRepository;
import ar.outfitmaker.specification.GarmentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GarmentService {

    private final GarmentRepository garmentRepository;

    public GarmentService(GarmentRepository garmentRepository) {
        this.garmentRepository = garmentRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<GarmentDTO> getGarments(GarmentFilters garmentFilters, Pageable pageable) {
        // Query w/ Specification Filters and Paging
        Specification<Garment> spec = GarmentSpecification.byCriteria(garmentFilters);
        Page<Garment> page = garmentRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(GarmentDTO::from));
    }
}
