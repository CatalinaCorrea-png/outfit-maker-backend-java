package ar.outfitmaker.service.auth;

import ar.outfitmaker.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class TokenService {

    private final SecretKey secretKey;

    public TokenService(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.key().getBytes(StandardCharsets.UTF_8));
    }

    public String generate(UserDetails userDetails, Date expirationDate) {
        return generate(userDetails, expirationDate, Map.of());
    }

    public String generate(UserDetails userDetails, Date expirationDate, Map<String, ?> additionalClaims) {
        return Jwts.builder()
                .claims()
                .subject(userDetails.getUsername())
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expirationDate)
                .add(additionalClaims)
                .and()
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {
        return getAllClaims(token).getSubject();
    }

    public boolean isExpired(String token) {
        return getAllClaims(token).getExpiration().before(new Date(System.currentTimeMillis()));
    }

    public boolean isValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return Objects.equals(userDetails.getUsername(), email) && !isExpired(token);
    }

    // verify the token
    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
