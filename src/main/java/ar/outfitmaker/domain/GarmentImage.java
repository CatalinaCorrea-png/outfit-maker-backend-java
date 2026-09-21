package ar.outfitmaker.domain;

import ar.outfitmaker.repository.RepositoryElement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "garment_images")
public class GarmentImage implements RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garment_id", nullable = false)
    private Garment garment;

    // URL en S3 o el storage que uses ??
    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    /** Requerido por JPA. No usar desde el código. */
    protected GarmentImage() {}

    public GarmentImage(Garment garment, String imageUrl, int sortOrder) {
        this.garment = garment;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
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
        // TODO() Sin reglas de negocio propias por ahora
    }

    public Garment getGarment() {
        return garment;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
