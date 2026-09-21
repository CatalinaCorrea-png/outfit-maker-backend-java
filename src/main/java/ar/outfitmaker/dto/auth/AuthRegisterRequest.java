package ar.outfitmaker.dto.auth;

public record AuthRegisterRequest(
        String name,
        String email,
        String password
) {
}
