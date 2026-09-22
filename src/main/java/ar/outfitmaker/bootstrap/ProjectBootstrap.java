package ar.outfitmaker.bootstrap;

import ar.outfitmaker.domain.Category;
import ar.outfitmaker.domain.Fit;
import ar.outfitmaker.domain.Garment;
import ar.outfitmaker.domain.GarmentImage;
import ar.outfitmaker.domain.Pattern;
import ar.outfitmaker.domain.Season;
import ar.outfitmaker.domain.Slot;
import ar.outfitmaker.domain.User;
import ar.outfitmaker.repository.CategoryRepository;
import ar.outfitmaker.repository.GarmentRepository;
import ar.outfitmaker.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Profile("!test")
public class ProjectBootstrap implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ProjectBootstrap.class);

    // ─── Repositories ──────────────────────────────────────────────────────
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final GarmentRepository garmentRepository;
    private final PasswordEncoder encoder;

    // ─── Users ─────────────────────────────────────────────────────────────
    private User cher;
    private User dionne;

    // ─── Categories ────────────────────────────────────────────────────────
    private Category remera;
    private Category pantalon;
    private Category campera;
    private Category zapatillas;

    // ─── Garments ──────────────────────────────────────────────────────────
    private Garment remeraRayada;
    private Garment jeanNegro;
    private Garment camperaDeJean;
    private Garment zapatillasBlancas;

    public ProjectBootstrap(UserRepository userRepository,
                            CategoryRepository categoryRepository,
                            GarmentRepository garmentRepository,
                            PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.garmentRepository = garmentRepository;
        this.encoder = encoder;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Creation Methods
    // ═════════════════════════════════════════════════════════════════════════

    public void createUser(User user) {
        Optional<User> userEnRepo = userRepository.findByEmail(user.getEmail());
        if (userEnRepo.isPresent()) {
            user.setId(userEnRepo.get().getId());
        } else {
            userRepository.save(user);
            log.info("User {} creado", user.getName());
        }
    }

    public void createCategory(Category category) {
        Optional<Category> categoryEnRepo = categoryRepository.findByName(category.getName());
        if (categoryEnRepo.isPresent()) {
            category.setId(categoryEnRepo.get().getId());
        } else {
            categoryRepository.save(category);
            log.info("Category {} creada", category.getName());
        }
    }

    public void createGarment(Garment garment) {
        Optional<Garment> garmentEnRepo =
                garmentRepository.findByUserAndName(garment.getUser(), garment.getName());
        if (garmentEnRepo.isPresent()) {
            garment.setId(garmentEnRepo.get().getId());
        } else {
            garmentRepository.save(garment);
            log.info("Garment {} creada", garment.getName());
        }
    }

    // Las imágenes se persisten solas por el cascade de Garment.images,
    // así que hay que colgarlas antes del createGarment.
    // El orden de los parámetros es el sortOrder: la primera es la portada.
    public void addImages(Garment garment, String... imageUrls) {
        for (int index = 0; index < imageUrls.length; index++) {
            garment.addImage(new GarmentImage(garment, imageUrls[index], index));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Initialization Methods
    // ═════════════════════════════════════════════════════════════════════════

    public void initUsers() {
        cher = new User(
                "cher@gmail.com",
                "Cher",
                "https://media.vogue.mx/photos/5c7732b041573ab604c65dc1/2:3/w_2560%2Cc_limit/GettyImages-159835758.jpg",
                encoder.encode("123")
        );

        dionne = new User(
                "dionne@gmail.com",
                "Dionne",
                "https://mx.pinterest.com/pin/646055509080216998/",
                encoder.encode("123")
        );

        List.of(cher, dionne).forEach(this::createUser);
    }

    public void initCategories() {
        remera = new Category("Remera", Slot.UPPER);
        pantalon = new Category("Pantalón", Slot.LOWER);
        campera = new Category("Campera", Slot.OUTERWEAR);
        zapatillas = new Category("Zapatillas", Slot.FOOTWEAR);

        List.of(remera, pantalon, campera, zapatillas).forEach(this::createCategory);
    }

    public void initGarments() {
        remeraRayada = Garment.builder(cher, remera)
                .name("Remera rayada")
                .brand("Zara")
                .primaryColor("#ffffff")
                .secondaryColor("navy")
                .pattern(Pattern.STRIPED)
                .material("algodón")
                .formality(2)
                .fit(Fit.REGULAR)
                .season(Season.SUMMER)
                .careNotes("Lavar con agua fría")
                .build();

        jeanNegro = Garment.builder(dionne, pantalon)
                .name("Jean negro")
                .brand("Levi's")
                .primaryColor("#1c1c1c")
                .pattern(Pattern.SOLID)
                .material("denim")
                .formality(3)
                .fit(Fit.SLIM)
                .season(Season.ALL_SEASONS)
                .build();

        camperaDeJean = Garment.builder(cher, campera)
                .name("Campera de jean")
                .brand("Levi's")
                .primaryColor("#5b7ba6")
                .pattern(Pattern.SOLID)
                .material("denim")
                .formality(2)
                .fit(Fit.OVERSIZED)
                .season(Season.MID_SEASON)
                .careNotes("No usar secarropas")
                .build();

        zapatillasBlancas = Garment.builder(dionne, zapatillas)
                .name("Zapatillas blancas")
                .brand("Adidas")
                .primaryColor("#ffffff")
                .pattern(Pattern.SOLID)
                .material("cuero")
                .formality(2)
                .fit(Fit.REGULAR)
                .season(Season.ALL_SEASONS)
                .build();

        addImages(remeraRayada,
                "https://picsum.photos/seed/remera-rayada-1/600/750",
                "https://picsum.photos/seed/remera-rayada-2/600/750");
        addImages(jeanNegro, "https://picsum.photos/seed/jean-negro-1/600/750");
        addImages(camperaDeJean,
                "https://picsum.photos/seed/campera-jean-1/600/750",
                "https://picsum.photos/seed/campera-jean-2/600/750",
                "https://picsum.photos/seed/campera-jean-3/600/750");
        // zapatillasBlancas queda sin fotos a propósito: es el caso de la card sin imagen

        List.of(remeraRayada, jeanNegro, camperaDeJean, zapatillasBlancas)
                .forEach(this::createGarment);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // InitializingBean
    // ═════════════════════════════════════════════════════════════════════════
    @Override
    public void afterPropertiesSet() {
        log.info("************************************************************************");
        log.info("Running initialization");
        log.info("************************************************************************");

        initUsers();
        initCategories();
        initGarments();

        log.info("------------------------------------------------------------------------");
    }
}