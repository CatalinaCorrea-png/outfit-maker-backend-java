package ar.outfitmaker.domain;

import ar.outfitmaker.repository.RepositoryElement;
import jakarta.persistence.*;

/** Cada prenda dentro de un outfit, con su orden de capa. */
@Entity
@Table(name = "outfit_items")
public class OutfitItem implements RepositoryElement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_id", nullable = false)
    private Outfit outfit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garment_id", nullable = false)
    private Garment garment;

    // Orden de capas: 0 = base (remera), 1 = medio (camisa), 2 = exterior (campera)
    @Column(name = "layer_order", nullable = false)
    private int layerOrder = 0;

    /** Requerido por JPA. No usar desde el código. */
    protected OutfitItem() {
    }

    public OutfitItem(Outfit outfit, Garment garment, int layerOrder) {
        this.outfit = outfit;
        this.garment = garment;
        this.layerOrder = layerOrder;
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

    public Outfit getOutfit() {
        return outfit;
    }

    public Garment getGarment() {
        return garment;
    }

    public int getLayerOrder() {
        return layerOrder;
    }
}
