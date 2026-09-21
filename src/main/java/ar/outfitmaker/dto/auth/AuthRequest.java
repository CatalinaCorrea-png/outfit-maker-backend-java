package ar.outfitmaker.dto.auth;

/** REQUEST - Lo que recibe el endpoint */
public record AuthRequest(
        String email,
        String password
) {
}
