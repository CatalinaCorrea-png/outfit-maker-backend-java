package ar.outfitmaker.dto;

import ar.outfitmaker.domain.Pattern;
import ar.outfitmaker.domain.Season;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Los campos de filtrado pueden ser null a propósito: las Specifications ignoran
 * el filtro cuando su valor es null, o sea que cada uno es opcional.
 */
public record GarmentFilters(
        // filtering
        String userId,
        String category,
        String name,
        String brand,
        Pattern pattern,
        Integer formality,
        Season season,
        Boolean active,
        // paging & sorting
        Integer page,
        Integer pageSize,
        String sortBy, // "name", "createdAt"
        Boolean ascending
) {
    public GarmentFilters {
        page = (page == null) ? 0 : page;
        pageSize = (pageSize == null) ? 6 : pageSize;
        sortBy = (sortBy == null) ? "createdAt" : sortBy;
        ascending = (ascending == null) || ascending;
    }

    public Pageable toPageable() {
        Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, pageSize, Sort.by(direction, sortBy));
    }
}
