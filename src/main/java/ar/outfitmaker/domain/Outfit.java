package ar.outfitmaker.domain;

import ar.outfitmaker.repository.RepositoryElement;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "outfits")
public class Outfit implements RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;

    private String notes;

    // ¿Lo armó la IA o el usuario manualmente?
    @Column(name = "ai_generated", nullable = false)
    private boolean aiGenerated = false;

    // El prompt que usó el usuario si fue generado por IA
    @Column(name = "ai_prompt")
    private String aiPrompt;

    // Rating del usuario (1-5) para feedback loop
    private Integer rating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt = LocalDate.now();

    // Relaciones
    @OneToMany(mappedBy = "outfit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutfitItem> items = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "outfit_tags",
            joinColumns = @JoinColumn(name = "outfit_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new LinkedHashSet<>();

    /** Requerido por JPA. No usar desde el código. */
    protected Outfit() {
    }

    public Outfit(User user) {
        this.user = user;
    }

    /** Outfit armado por la IA a partir de un prompt del usuario. */
    public static Outfit aiGenerated(User user, String aiPrompt) {
        Outfit outfit = new Outfit(user);
        outfit.aiGenerated = true;
        outfit.aiPrompt = aiPrompt;
        return outfit;
    }

    public void addOutfitItem(OutfitItem item) {
        items.add(item);
    }

    public void deleteOutfitItem(OutfitItem item) {
        items.remove(item);
    }

    public void addTag(Tag tag) {
        if (tags.contains(tag)) {
            throw new IllegalArgumentException("Tag already exists");
        }
        tags.add(tag);
    }

    public void deleteTag(Tag tag) {
        tags.remove(tag);
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public String getAiPrompt() {
        return aiPrompt;
    }

    public Integer getRating() {
        return rating;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public List<OutfitItem> getItems() {
        return items;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
