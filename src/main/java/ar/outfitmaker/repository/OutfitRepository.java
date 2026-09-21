package ar.outfitmaker.repository;

import ar.outfitmaker.domain.Outfit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutfitRepository extends CrudRepository<Outfit, String> {

    // TODO: reemplazar por Specifications cuando OutfitFilters tenga criterios reales
    @EntityGraph(attributePaths = {"user", "items", "items.garment", "items.garment.category", "items.garment.images", "tags"})
    @Query("SELECT o FROM Outfit o")
    Page<Outfit> findAllBy(Pageable pageable);
}
