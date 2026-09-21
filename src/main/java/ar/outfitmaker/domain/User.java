package ar.outfitmaker.domain;

import ar.outfitmaker.repository.RepositoryElement;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements RepositoryElement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email = "";

    @Column(name = "name", nullable = false)
    private String name = "";

    @Column(name = "avatar_url")
    private String avatarUrl = "";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt = LocalDate.now();

    private String password = "";

    // Relaciones (lazy por defecto en @OneToMany)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Garment> garments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Outfit> outfits = new ArrayList<>();

    /** Requerido por JPA. No usar desde el código. */
    protected User() {}

    public User(String email, String name, String avatarUrl, String password) {
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.password = password;
    }

    public void addGarment(Garment garment) {
        garments.add(garment);
    }

    public void deleteGarment(Garment garment) {
        garments.remove(garment);
    }

    public void addOutfit(Outfit outfit) {
        outfits.add(outfit);
    }

    public void deleteOutfit(Outfit outfit) {
        outfits.remove(outfit);
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
        // TODO() not yet implemented
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Garment> getGarments() {
        return garments;
    }

    public List<Outfit> getOutfits() {
        return outfits;
    }
}
