package ar.outfitmaker.service;

import ar.outfitmaker.domain.Outfit;
import ar.outfitmaker.dto.OutfitDTO;
import ar.outfitmaker.dto.OutfitFilters;
import ar.outfitmaker.dto.PageResponse;
import ar.outfitmaker.repository.OutfitRepository;
import ar.outfitmaker.service.storage.PhotoStorage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutfitService {

    private final OutfitRepository outfitRepository;
    private final PhotoStorage photoStorage;

    public OutfitService(OutfitRepository outfitRepository, PhotoStorage photoStorage) {
        this.outfitRepository = outfitRepository;
        this.photoStorage = photoStorage;
    }

    @Transactional(readOnly = true)
    public PageResponse<OutfitDTO> getOutfits(OutfitFilters outfitFilters, Pageable pageable) {
        Page<Outfit> page = outfitRepository.findAllBy(pageable);
        return PageResponse.from(page.map(outfit -> OutfitDTO.from(outfit, photoStorage::urlOf)));
    }
}
