package ar.outfitmaker.dto.auth;

// named Pair<String, String>
public record TokenPair(String accessToken, String refreshToken) {}
