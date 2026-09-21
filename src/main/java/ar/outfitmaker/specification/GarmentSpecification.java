package ar.outfitmaker.specification;

import ar.outfitmaker.domain.Garment;
import ar.outfitmaker.domain.Pattern;
import ar.outfitmaker.domain.Season;
import ar.outfitmaker.dto.GarmentFilters;
import org.springframework.data.jpa.domain.Specification;

public class GarmentSpecification {
    private GarmentSpecification() {}

    public static Specification<Garment> userIdEqual(String userId) {
        return (root, query, cb) ->
                userId == null ? null : cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Garment> categoryLike(String category) {
        return (root, query, cb) ->
                category == null ? null : cb.like(cb.lower(root.get("category").get("name")), contains(category));
    }

    public static Specification<Garment> nameLike(String name) {
        return (root, query, cb) ->
                name == null ? null : cb.like(cb.lower(root.get("name")), contains(name));
    }

    public static Specification<Garment> brandLike(String brand) {
        return (root, query, cb) ->
                brand == null ? null : cb.like(cb.lower(root.get("brand")), contains(brand));
    }

    public static Specification<Garment> patternEqual(Pattern pattern) {
        return (root, query, cb) ->
                pattern == null ? null : cb.equal(root.get("pattern"), pattern);
    }

    // 1 = deportivo, 2 = casual, 3 = smart casual, 4 = semi formal, 5 = formal
    public static Specification<Garment> formalityEqual(Integer formality) {
        return (root, query, cb) ->
                formality == null ? null : cb.equal(root.get("formality"), formality);
    }

    // ALL_SEASONS entra siempre: sirve para cualquier temporada pedida
    public static Specification<Garment> seasonEqual(Season season) {
        return (root, query, cb) -> {
            if (season == null) {
                return null;
            }
            if (season == Season.ALL_SEASONS) {
                return cb.equal(root.get("season"), Season.ALL_SEASONS);
            }
            return cb.or(
                    cb.equal(root.get("season"), season),
                    cb.equal(root.get("season"), Season.ALL_SEASONS)
            );
        };
    }
    // Soft delete: active = false -> prenda donada/vendida
    public static Specification<Garment> activeEqual(Boolean active) {
        return (root, query, cb) ->
                active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<Garment> onlyActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    /** Compone todas las specs a partir de los filtros recibidos. */
    public static Specification<Garment> byCriteria(GarmentFilters criteria) {
        // si no se pide explícitamente, solo se listan las prendas activas
        Specification<Garment> activeSpec = criteria.active() == null
                ? onlyActive()
                : activeEqual(criteria.active());

        return userIdEqual(criteria.userId())
                .and(categoryLike(criteria.category()))
                .and(nameLike(criteria.name()))
                .and(brandLike(criteria.brand()))
                .and(patternEqual(criteria.pattern()))
                .and(formalityEqual(criteria.formality()))
                .and(seasonEqual(criteria.season()))
                .and(activeSpec);
    }


    private static String contains(String value) {
        return "%" + value.toLowerCase() + "%";
    }
}
