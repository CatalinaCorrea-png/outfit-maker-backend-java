package ar.outfitmaker.repository;

import ar.outfitmaker.domain.Category;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends CrudRepository<Category, String> {
    Optional<Category> findByName(String name);

    // El orden lo resuelve la base: así el select llega ordenado
    List<Category> findAllByOrderByNameAsc();
}
