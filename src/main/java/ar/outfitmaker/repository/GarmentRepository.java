package ar.outfitmaker.repository;

import ar.outfitmaker.domain.Garment;
import ar.outfitmaker.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GarmentRepository
        extends CrudRepository<Garment, String>,
        JpaSpecificationExecutor<Garment> {

    @Override
    @EntityGraph(attributePaths = {"user", "category", "images"})
    Page<Garment> findAll(Specification<Garment> spec, Pageable pageable);

    Optional<Garment> findByUserAndName(User user, String name);
}
