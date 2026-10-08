package ar.outfitmaker.service.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.outfitmaker.repository.GarmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class OrphanPhotoSweeperTest {

    private final FakePhotoStorage photoStorage = new FakePhotoStorage();
    private final GarmentRepository garmentRepository = mock(GarmentRepository.class);
    private OrphanPhotoSweeper sweeper;

    @BeforeEach
    void setup() {
        sweeper = new OrphanPhotoSweeper(photoStorage, garmentRepository);
    }

    @Test
    void sweep_deletesOnlyThePhotosNoGarmentUses() {
        photoStorage.oldKeys.addAll(List.of("usada.webp", "huerfana.webp"));
        // Una prenda con foto de adelante y sin foto de atrás: esas columnas vienen en null
        List<Object[]> columns = List.<Object[]>of(
                new Object[]{"usada.webp", null, null, null, null, null});
        when(garmentRepository.findAllPhotoKeyColumns()).thenReturn(columns);

        int deleted = sweeper.sweep();

        assertThat(deleted).isOne();
        assertThat(photoStorage.deleted).containsExactly("huerfana.webp");
    }

    @Test
    void sweep_withNothingOld_deletesNothing() {
        when(garmentRepository.findAllPhotoKeyColumns()).thenReturn(List.of());

        assertThat(sweeper.sweep()).isZero();
        assertThat(photoStorage.deleted).isEmpty();
    }
}
