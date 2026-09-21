package ar.outfitmaker.domain;

import ar.outfitmaker.repository.RepositoryElement;
import jakarta.persistence.*;

@Entity
@Table(name = "tags")
public class Tag implements RepositoryElement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagType type;

    /** Requerido por JPA. No usar desde el código. */
    protected Tag() {}

    public Tag(String name, TagType type) {
        this.name = name;
        this.type = type;
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

    public TagType getType() {
        return type;
    }

    public void setType(TagType type) {
        this.type = type;
    }

    @Override
    public void validate() {
        // TODO() Sin reglas de negocio propias por ahora
    }
}
