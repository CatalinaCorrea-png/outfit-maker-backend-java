package ar.outfitmaker.dto.auth;

public record AuthResponse(
        String name,
        String email,
        String id
) {
}
