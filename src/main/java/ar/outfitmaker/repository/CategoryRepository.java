package ar.outfitmaker.repository;

import ar.outfitmaker.domain.Category;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends CrudRepository<Category, String> {
    Optional<Category> findByName(String name);
}
