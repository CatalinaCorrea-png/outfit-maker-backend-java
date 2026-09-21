package ar.outfitmaker.dto.auth;

/** RESPONSE - Lo que devuelve el endpoint, lo que necesita el front */
public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        long expirationTime,
        String id,
        String name
) {
    public AuthenticationResponse withoutRefreshToken() {
        return new AuthenticationResponse(accessToken, "", expirationTime, id, name);
    }
}
