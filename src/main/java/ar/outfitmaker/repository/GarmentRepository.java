package ar.outfitmaker.repository;

import ar.outfitmaker.domain.Garment;
import ar.outfitmaker.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GarmentRepository
        extends CrudRepository<Garment, String>,
        JpaSpecificationExecutor<Garment> {

    @Override
    // Las fotos son columnas de garments (@Embedded): no van en el graph ni rompen la paginación
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Garment> findAll(Specification<Garment> spec, Pageable pageable);

    Optional<Garment> findByUserAndName(User user, String name);

    // Todas las URLs de fotos que usa alguna prenda (las 3 de cada cara). El barrido de
    // huérfanos no toca estas. Una cara sin foto trae sus columnas en null.
    @Query("""
            select g.front.key, g.front.thumbKey, g.front.cutoutKey,
                   g.back.key, g.back.thumbKey, g.back.cutoutKey
            from Garment g
            """)
    List<Object[]> findAllPhotoKeyColumns();
}
