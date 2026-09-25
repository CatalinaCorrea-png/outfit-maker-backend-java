package ar.outfitmaker.service;

import ar.outfitmaker.domain.Category;
import ar.outfitmaker.domain.Garment;
import ar.outfitmaker.domain.User;
import ar.outfitmaker.dto.GarmentCreateRequest;
import ar.outfitmaker.dto.GarmentDTO;
import ar.outfitmaker.dto.GarmentFilters;
import ar.outfitmaker.dto.PageResponse;
import ar.outfitmaker.errors.NotFoundException;
import ar.outfitmaker.repository.CategoryRepository;
import ar.outfitmaker.repository.GarmentRepository;
import ar.outfitmaker.repository.UserRepository;
import ar.outfitmaker.specification.GarmentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GarmentService {

    private final GarmentRepository garmentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public GarmentService(
            GarmentRepository garmentRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.garmentRepository = garmentRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<GarmentDTO> getGarments(GarmentFilters garmentFilters, Pageable pageable) {
        // Query w/ Specification Filters and Paging
        Specification<Garment> spec = GarmentSpecification.byCriteria(garmentFilters);
        Page<Garment> page = garmentRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(GarmentDTO::from));
    }

    @Transactional
    public GarmentDTO createGarment(GarmentCreateRequest garmentCreateRequest) {
        Garment newGarment = fromRequest(garmentCreateRequest);
        newGarment.validate();
        garmentRepository.save(newGarment);
        return GarmentDTO.from(newGarment);
    }


    private Garment fromRequest(GarmentCreateRequest garmentCreateRequest) {
        User user = userRepository
                .findById(garmentCreateRequest.userId())
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND",
                        "No se encuentra un usuario registrado con este email: " + garmentCreateRequest.userId()));

        Category category = categoryRepository
                .findByName(garmentCreateRequest.category())
                .orElseThrow(() -> new NotFoundException("GARMENT_NOT_FOUND", "No existe la categoria"));

        return Garment.builder(user, category).build();
    }
}
