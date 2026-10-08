package ar.outfitmaker.service;

import ar.outfitmaker.domain.Category;
import ar.outfitmaker.domain.Garment;
import ar.outfitmaker.domain.GarmentPhoto;
import ar.outfitmaker.domain.User;
import ar.outfitmaker.dto.GarmentCreateDTO;
import ar.outfitmaker.dto.GarmentDTO;
import ar.outfitmaker.dto.GarmentFilters;
import ar.outfitmaker.dto.GarmentPhotoFiles;
import ar.outfitmaker.dto.PageResponse;
import ar.outfitmaker.errors.ConflictException;
import ar.outfitmaker.errors.NotFoundException;
import ar.outfitmaker.repository.CategoryRepository;
import ar.outfitmaker.repository.GarmentRepository;
import ar.outfitmaker.repository.UserRepository;
import ar.outfitmaker.service.storage.PhotoFiles;
import ar.outfitmaker.service.storage.PhotoStorage;
import ar.outfitmaker.specification.GarmentSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class GarmentService {

    private final GarmentRepository garmentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PhotoStorage photoStorage;

    public GarmentService(
            GarmentRepository garmentRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            PhotoStorage photoStorage) {
        this.garmentRepository = garmentRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.photoStorage = photoStorage;
    }

    @Transactional(readOnly = true)
    public PageResponse<GarmentDTO> getGarments(GarmentFilters garmentFilters, String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND",
                        "No se encuentra un usuario registrado con el email: " + userEmail));

        // Query w/ Specification Filters and Paging
        Specification<Garment> spec = GarmentSpecification.byCriteria(garmentFilters, user.getId());
        Page<Garment> page = garmentRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(garment -> GarmentDTO.from(garment, photoStorage::urlOf)));
    }

    @Transactional
    /**
     * Alta atómica: nunca queda una prenda sin sus fotos ni una foto sin su prenda.
     *  Las fotos se escriben ANTES del commit: si la prenda existe, sus archivos también.
     *  Si la transacción no termina en commit, se borran las fotos que se hayan escrito.
     * El único hueco (que el proceso muera entre escribir y commitear) lo cubre OrphanPhotoSweeper.
     */
    public GarmentDTO createGarment(GarmentCreateDTO garmentCreateDTO, GarmentPhotoFiles photos, String userEmail) {
        // userEmail viene de SecurityContext (ya está validado)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND",
                        "No se encuentra un usuario registrado con el email: " + userEmail));
        Category category = categoryRepository.findByName(garmentCreateDTO.category())
                .orElseThrow(() -> new NotFoundException("CATEGORY_NOT_FOUND",
                        "No se encontró la categoría: " + garmentCreateDTO.category()));

        if (garmentRepository.findByUserAndName(user, garmentCreateDTO.name()).isPresent()) {
            throw new ConflictException("GARMENT_ALREADY_EXISTS",
                    "Prenda '" + garmentCreateDTO.name() + "' de '" + user.getName() + "' ya fue registrada");
        }

        // 1. Todos los archivos se validan antes de escribir el primero
        photos.toStore().forEach(PhotoFiles::validate);

        // 3. Registrado antes de escribir: si falla la segunda foto, la primera también se borra
        List<String> writtenKeys = deleteOnRollback();

        // 2. Las fotos se escriben dentro de la transacción, antes del commit
        GarmentPhoto front = storePhoto(photos.front(), photos.frontThumb(), writtenKeys);
        GarmentPhoto back = photos.back() == null ? null : storePhoto(photos.back(), photos.backThumb(), writtenKeys);

        Garment garment = Garment.builder(user, category)
                .name(garmentCreateDTO.name())
                .brand(garmentCreateDTO.brand())
                .primaryColor(garmentCreateDTO.primaryColor())
                .secondaryColor(garmentCreateDTO.secondaryColor())
                .pattern(garmentCreateDTO.pattern())
                .material(garmentCreateDTO.material())
                .formality(garmentCreateDTO.formality())
                .fit(garmentCreateDTO.fit())
                .season(garmentCreateDTO.season())
                .careNotes(garmentCreateDTO.careNotes())
                .front(front)
                .back(back)
                .build();
        garment.validate();

        garmentRepository.save(garment);

        return GarmentDTO.from(garment, photoStorage::urlOf);
    }

    /**
     * Engancha a la transacción actual un "si no hay commit, borrá estas fotos".
     * Devuelve la lista a la que hay que ir sumando cada URL que se escribe.
     */
    private List<String> deleteOnRollback() {
        List<String> writtenKeys = new ArrayList<>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) writtenKeys.forEach(photoStorage::delete);
            }
        });
        return writtenKeys;
    }

    private GarmentPhoto storePhoto(MultipartFile original, MultipartFile thumb, List<String> writtenKeys) {
        String key = store(original, writtenKeys);
        String thumbKey = thumb == null ? null : store(thumb, writtenKeys);
        // El recorte sin fondo todavía no existe
        return new GarmentPhoto(key, thumbKey, null);
    }

    private String store(MultipartFile file, List<String> writtenKeys) {
        String key = photoStorage.save(file);
        writtenKeys.add(key);
        return key;
    }
}
