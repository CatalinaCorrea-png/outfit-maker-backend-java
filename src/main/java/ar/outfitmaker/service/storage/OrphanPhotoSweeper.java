package ar.outfitmaker.service.storage;

import ar.outfitmaker.repository.GarmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Red de seguridad del alta atómica: borra las fotos que no usa ninguna prenda.
 * El rollback ya limpia los errores normales; esto cubre el único hueco que queda,
 * que el proceso muera entre escribir las fotos y commitear la prenda.
 */
@Service
public class OrphanPhotoSweeper {

    private static final Logger log = LoggerFactory.getLogger(OrphanPhotoSweeper.class);

    // Mucho más que cualquier alta en curso: una foto recién escrita todavía no está
    // commiteada, y no tiene que parecer huérfana.
    static final Duration MIN_AGE = Duration.ofHours(1);

    private final PhotoStorage photoStorage;
    private final GarmentRepository garmentRepository;

    public OrphanPhotoSweeper(PhotoStorage photoStorage, GarmentRepository garmentRepository) {
        this.photoStorage = photoStorage;
        this.garmentRepository = garmentRepository;
    }

    /** Devuelve cuántas borró (el scheduler lo ignora; sirve para los tests). */
    @Scheduled(initialDelay = 5, fixedDelay = 60, timeUnit = TimeUnit.MINUTES)
    @Transactional(readOnly = true)
    public int sweep() {
        Set<String> referenced = garmentRepository.findAllPhotoKeyColumns().stream()
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .collect(Collectors.toSet());

        List<String> orphans = photoStorage.findKeysOlderThan(MIN_AGE).stream()
                .filter(key -> !referenced.contains(key))
                .toList();

        orphans.forEach(photoStorage::delete);
        if (!orphans.isEmpty()) log.info("Barrido de fotos: {} huérfana(s) borrada(s)", orphans.size());
        return orphans.size();
    }
}
