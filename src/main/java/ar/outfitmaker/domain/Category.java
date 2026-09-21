package ar.outfitmaker.domain;

import ar.outfitmaker.repository.RepositoryElement;
import jakarta.persistence.*;

@Entity
@Table(name="categories")
public class Category implements RepositoryElement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Slot slot;

    /** Requqerido por JPA. No usar desde el codigo. */
    protected Category() {}

    public Category(String name, Slot slot) {
        this.name = name;
        this.slot = slot;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    @Override
    public void validate() {
        // TODO() Sin reglas de negocio propias por ahora
    }
}
