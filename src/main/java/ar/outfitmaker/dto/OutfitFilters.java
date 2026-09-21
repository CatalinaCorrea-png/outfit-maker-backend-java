package ar.outfitmaker.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record OutfitFilters(
        // paging & sorting
        Integer page,
        Integer pageSize,
        String sortBy,
        Boolean ascending

) {
    public OutfitFilters {
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
