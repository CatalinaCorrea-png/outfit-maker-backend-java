package ar.outfitmaker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jwt")
public record JwtProperties(
        String key,
        long accessTokenExpiration,
        long refreshTokenExpiration
) {
}
