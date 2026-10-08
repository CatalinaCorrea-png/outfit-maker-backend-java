package ar.outfitmaker.specification;

import static org.assertj.core.api.Assertions.assertThat;

import ar.outfitmaker.domain.*;
import ar.outfitmaker.dto.GarmentFilters;
import ar.outfitmaker.repository.GarmentRepository;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GarmentSpecificationTest {

    @Autowired
    private GarmentRepository garmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private User cher;
    private User dionne;
    private Category shirt;
    private Category pants;

    @BeforeEach
    void setup() {
        cher = entityManager.persistAndFlush(new User("cher@gmail.com", "Cher", "", "hash"));
        dionne = entityManager.persistAndFlush(new User("dionne@gmail.com", "Dionne", "", "hash"));
        shirt = entityManager.persistAndFlush(new Category("Remera", Slot.UPPER));
        pants = entityManager.persistAndFlush(new Category("Pantalón", Slot.LOWER));
    }

    // ─── filtros individuales ───────────────────────────────────────────────

    @Test
    void nameLike_matchesPartialAndIsCaseInsensitive() {
        persistGarment(cher, shirt, "Remera rayada", Season.SUMMER, true);
        persistGarment(cher, shirt, "Camisa blanca", Season.SUMMER, true);
        List<Garment> found = garmentRepository.findAll(GarmentSpecification.nameLike("RAYA"));

        assertThat(found).extracting(Garment::getName).containsExactly("Remera rayada");
    }

    @Test
    void categoryLike_filtersByCategoryName() {
        persistGarment(cher, shirt, "Remera rayada", Season.SUMMER, true);
        persistGarment(cher, pants, "Jean negro", Season.ALL_SEASONS, true);

        List<Garment> found = garmentRepository.findAll(GarmentSpecification.categoryLike("pantal"));

        assertThat(found).extracting(Garment::getName).containsExactly("Jean negro");
    }

    @Test
    void belongsTo_isolatesEachUsersWardrobe() {
        persistGarment(cher, shirt, "Remera de Cher", Season.SUMMER, true);
        persistGarment(dionne, shirt, "Remera de Dionne", Season.SUMMER, true);

        List<Garment> found = garmentRepository.findAll(GarmentSpecification.belongsTo(cher.getId()));

        assertThat(found).extracting(Garment::getName).containsExactly("Remera de Cher");
    }

    // ─── la regla de las temporadas ─────────────────────────────────────────

    @Test
    void seasonEqual_summerAlsoReturnsAllSeasonsGarments() {
        persistGarment(cher, shirt, "Remera de verano", Season.SUMMER, true);
        persistGarment(cher, shirt, "Remera de todo el año", Season.ALL_SEASONS, true);
        persistGarment(cher, shirt, "Buzo de invierno", Season.WINTER, true);

        List<Garment> found = garmentRepository.findAll(GarmentSpecification.seasonEqual(Season.SUMMER));

        assertThat(found).extracting(Garment::getName)
                .containsExactlyInAnyOrder("Remera de verano", "Remera de todo el año");
    }

    @Test
    void seasonEqual_allSeasonsReturnsOnlyAllSeasonsGarments() {
        persistGarment(cher, shirt, "Remera de verano", Season.SUMMER, true);
        persistGarment(cher, shirt, "Remera de todo el año", Season.ALL_SEASONS, true);

        List<Garment> found = garmentRepository.findAll(GarmentSpecification.seasonEqual(Season.ALL_SEASONS));

        assertThat(found).extracting(Garment::getName).containsExactly("Remera de todo el año");
    }

    // ─── soft delete ────────────────────────────────────────────────────────

    @Test
    void byCriteria_excludesInactiveGarmentsWhenActiveIsNotRequested() {
        persistGarment(cher, shirt, "Remera activa", Season.SUMMER, true);
        persistGarment(cher, shirt, "Remera donada", Season.SUMMER, false);

        List<Garment> found = garmentRepository.findAll(GarmentSpecification.byCriteria(filtersWithActive(null), cher.getId()));

        assertThat(found).extracting(Garment::getName).containsExactly("Remera activa");
    }

    @Test
    void byCriteria_returnsInactiveGarmentsWhenExplicitlyRequested() {
        persistGarment(cher, shirt, "Remera activa", Season.SUMMER, true);
        persistGarment(cher, shirt, "Remera donada", Season.SUMMER, false);

        List<Garment> found = garmentRepository.findAll(GarmentSpecification.byCriteria(filtersWithActive(false), cher.getId()));

        assertThat(found).extracting(Garment::getName).containsExactly("Remera donada");
    }

    // ─── composición, paginado y orden ──────────────────────────────────────

    @Test
    void byCriteria_combinesFiltersWithAnd() {
        persistGarment(cher, shirt, "Remera de verano", Season.SUMMER, true);
        persistGarment(dionne, shirt, "Remera de verano", Season.SUMMER, true);
        persistGarment(cher, pants, "Jean de verano", Season.SUMMER, true);

        GarmentFilters filters = new GarmentFilters(
                "remera", null, null, null, null, Season.SUMMER, null,
                null, null, null, null);

        List<Garment> found = garmentRepository.findAll(GarmentSpecification.byCriteria(filters, cher.getId()));

        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getUser().getId()).isEqualTo(cher.getId());
    }

    @Test
    void findAll_paginatesAndSortsByName() {
        persistGarment(cher, shirt, "C remera", Season.SUMMER, true);
        persistGarment(cher, shirt, "A remera", Season.SUMMER, true);
        persistGarment(cher, shirt, "B remera", Season.SUMMER, true);

        GarmentFilters filters = new GarmentFilters(
                null, null, null, null, null, null, null,
                0, 2, "name", true);

        Page<Garment> page = garmentRepository.findAll(
                GarmentSpecification.byCriteria(filters, cher.getId()), filters.toPageable());

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Garment::getName).containsExactly("A remera", "B remera");
    }

    @Test
    void findByUserAndName_onlyFindsTheGarmentOfThatUser() {
        persistGarment(cher, shirt, "Remera rayada", Season.SUMMER, true);
        persistGarment(dionne, shirt, "Remera rayada", Season.SUMMER, true);

        assertThat(garmentRepository.findByUserAndName(cher, "Remera rayada")).isPresent();
        assertThat(garmentRepository.findByUserAndName(dionne, "Otra cosa")).isEmpty();
    }

    // ─── fotos embebidas ────────────────────────────────────────────────────

    @Test
    void photos_roundTripThroughTheDatabase() {
        Garment saved = entityManager.persistAndFlush(Garment.builder(cher, shirt)
                .name("Remera con fotos")
                .primaryColor("navy")
                .formality(2)
                .front(new GarmentPhoto("front.jpg", "front-thumb.jpg", "front-cutout.webp"))
                .back(new GarmentPhoto("back.jpg", null, null))
                .build());
        entityManager.clear(); // obliga a hidratar desde la base, no desde el cache

        Garment found = garmentRepository.findById(saved.getId()).orElseThrow();

        // Hibernate arma el record con el constructor canónico a partir de las columnas front_*
        assertThat(found.getFront()).contains(
                new GarmentPhoto("front.jpg", "front-thumb.jpg", "front-cutout.webp"));
        // Atrás: las columnas que vinieron en null siguen en null dentro del record
        assertThat(found.getBack()).contains(new GarmentPhoto("back.jpg", null, null));
    }

    @Test
    void photos_withAllColumnsNullComeBackAsEmpty() {
        Garment saved = entityManager.persistAndFlush(Garment.builder(cher, shirt)
                .name("Remera sin fotos")
                .primaryColor("navy")
                .formality(2)
                .build());
        entityManager.clear();

        Garment found = garmentRepository.findById(saved.getId()).orElseThrow();

        // Las tres columnas en null: Hibernate deja el embebido en null y el getter da vacío
        assertThat(found.getFront()).isEmpty();
        assertThat(found.getBack()).isEmpty();
    }

    // ─── claves en uso (para el barrido de huérfanas) ────────────────────────

    @Test
    void findAllPhotoKeyColumns_bringsTheSixKeysOfEachGarment() {
        entityManager.persistAndFlush(Garment.builder(cher, shirt)
                .name("Con foto adelante")
                .primaryColor("navy")
                .formality(2)
                .front(new GarmentPhoto("front.webp", "front-thumb.webp", null))
                .build());
        persistGarment(cher, shirt, "Sin fotos", Season.SUMMER, true);
        entityManager.clear();

        List<Object[]> rows = garmentRepository.findAllPhotoKeyColumns();

        // Una fila por prenda, con las 3 claves de adelante y las 3 de atrás; lo que falta, en null
        assertThat(rows).hasSize(2);
        assertThat(rows).anySatisfy(row ->
                assertThat(row).containsExactly("front.webp", "front-thumb.webp", null, null, null, null));
        assertThat(rows).anySatisfy(row -> assertThat(row).containsOnlyNulls());
    }

    // ─── N + 1 ────────────────────────────────────────────────────────────

    @Test
    void findAll_fetchesUserAndCategoryWithoutNPlusOne() {
        persistGarment(cher, shirt, "Remera A", Season.SUMMER, true);
        persistGarment(cher, shirt, "Remera B", Season.SUMMER, true);
        persistGarment(cher, pants, "Jean C", Season.SUMMER, true);
        entityManager.clear(); // vacía el cache de primer nivel: obliga a ir a la base

        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        Page<Garment> page = garmentRepository.findAll(
                GarmentSpecification.onlyActive(), PageRequest.of(0, 10));
        assertThat(page.getContent()).allSatisfy(garment -> {
            assertThat(garment.getCategory().getName()).isNotBlank();
            assertThat(garment.getUser().getEmail()).isNotBlank();
        }); // toca lo lazy

        // 1 sola consulta: el @EntityGraph trae user y category con JOINs (ManyToOne: no
        // multiplican filas, así que el LIMIT sigue en SQL), y las fotos son columnas de
        // garments. Spring Data se saltea el count porque la primera página no se llenó.
        assertThat(stats.getPrepareStatementCount()).isEqualTo(1); // datos + count
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private void persistGarment(User owner, Category category, String name, Season season, boolean active) {
        entityManager.persistAndFlush(Garment.builder(owner, category)
                .name(name)
                .brand("Zara")
                .formality(2)
                .season(season)
                .active(active)
                .build());
    }

    private static GarmentFilters filtersWithActive(Boolean active) {
        return new GarmentFilters(null, null, null, null, null, null, active,
                null, null, null, null);
    }
}
