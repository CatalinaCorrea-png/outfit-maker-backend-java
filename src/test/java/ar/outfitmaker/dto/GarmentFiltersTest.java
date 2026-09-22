package ar.outfitmaker.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GarmentFiltersTest {

    @Test
    void nullPagingParams_appliesDefaults() {
        GarmentFilters filters = new GarmentFilters(
                null, null, null, null, null, null, null, null,
                null, null, null, null);

        assertThat(filters.page()).isZero();
        assertThat(filters.pageSize()).isEqualTo(6);
        assertThat(filters.sortBy()).isEqualTo("createdAt");
        assertThat(filters.ascending()).isTrue();
    }

    @Test
    void givenPagingParams_keepsThem() {
        GarmentFilters filters = new GarmentFilters(
                null, null, null, null, null, null, null, null,
                2, 10, "name", false);

        assertThat(filters.page()).isEqualTo(2);
        assertThat(filters.pageSize()).isEqualTo(10);
        assertThat(filters.sortBy()).isEqualTo("name");
        assertThat(filters.ascending()).isFalse();
    }
}
