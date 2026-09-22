package ar.outfitmaker.domain;

import ar.outfitmaker.errors.BusinessException;
import ar.outfitmaker.repository.RepositoryElement;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "garments")
public class Garment implements RepositoryElement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name = "";

    private String brand = "";

    // Colores como hex o nombre normalizado: "navy", "#1a237e"
    @Column(name = "primary_color", nullable = false)
    private String primaryColor = "";

    @Column(name = "secondary_color", nullable = false)
    private String secondaryColor = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Pattern pattern = Pattern.SOLID;

    // "algodón", "lino", "cuero", "poliéster"
    private String material = "";

    // 1 = deportivo, 2 = casual, 3 = smart casual, 4 = semi formal, 5 = formal
    @Column(nullable = false)
    private int formality = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Fit fit = Fit.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Season season = Season.ALL_SEASONS;

    @Column(name = "care_notes")
    private String careNotes;

    // Soft delete: false = prenda donada/vendida, pero los outfits históricos quedan
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt = LocalDate.now();

    @OneToMany(mappedBy = "garment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GarmentImage> images = new ArrayList<>();

    /** Requerido por JPA. No usar desde el código. */
    protected Garment() {}

    public Garment(User user, Category category) {
        this.user = user;
        this.category = category;
    }

    public static Builder builder(User user, Category category) {
        return new Builder(user, category);
    }

    public void addImage(GarmentImage image) {
        images.add(image);
    }

    public void deleteImage(GarmentImage image) {
        images.remove(image);
    }

    public Optional<GarmentImage> primaryImage() {
        return images.stream()
                .min(Comparator.comparingInt(GarmentImage::getSortOrder));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void validate() {
        if (this.formality < 1 || this.formality > 5) throw new BusinessException("INVALID_FORMALITY_NUMBER", "La formalidad debe estar entre 1 y 5");
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getSecondaryColor() {
        return secondaryColor;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getMaterial() {
        return material;
    }

    public int getFormality() {
        return formality;
    }

    public Fit getFit() {
        return fit;
    }

    public Season getSeason() {
        return season;
    }

    public String getCareNotes() {
        return careNotes;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public List<GarmentImage> getImages() {
        return images;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public void setSecondaryColor(String secondaryColor) {
        this.secondaryColor = secondaryColor;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setFormality(int formality) {
        this.formality = formality;
    }

    public void setFit(Fit fit) {
        this.fit = fit;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    /** Clase static Builder para tener un constructor con argumentos nombrados */
    /** por estar adentro de Garment puede escribir los campos privados directamente. */
    public static final class Builder {
        public final Garment garment;

        private Builder(User user, Category category) {
            /** user y category son obligatorios: así el compilador te obliga a pasarlos.*/
            this.garment = new Garment(user, category);
        }

        public Builder name(String name) {
            garment.name = name;
            return this;
        }

        public Builder brand(String brand) {
            garment.brand = brand;
            return this;
        }

        public Builder primaryColor(String primaryColor) {
            garment.primaryColor = primaryColor;
            return this;
        }

        public Builder secondaryColor(String secondaryColor) {
            garment.secondaryColor = secondaryColor;
            return this;
        }

        public Builder pattern(Pattern pattern) {
            garment.pattern = pattern;
            return this;
        }

        public Builder material(String material) {
            garment.material = material;
            return this;
        }

        public Builder formality(int formality) {
            garment.formality = formality;
            return this;
        }

        public Builder fit(Fit fit) {
            garment.fit = fit;
            return this;
        }

        public Builder season(Season season) {
            garment.season = season;
            return this;
        }

        public Builder careNotes(String careNotes) {
            garment.careNotes = careNotes;
            return this;
        }

        public Builder active(boolean active) {
            garment.active = active;
            return this;
        }

        public Garment build() {
            return garment;
        }
    }
}
