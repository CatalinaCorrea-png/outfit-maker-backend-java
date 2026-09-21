package ar.outfitmaker.service.auth;

import ar.outfitmaker.config.JwtProperties;
import ar.outfitmaker.domain.User;
import ar.outfitmaker.dto.auth.AuthRequest;
import ar.outfitmaker.dto.auth.AuthenticationResponse;
import ar.outfitmaker.dto.auth.TokenPair;
import ar.outfitmaker.repository.RefreshTokenRepository;
import ar.outfitmaker.service.CustomUserDetailsService;
import ar.outfitmaker.service.UserService;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final CustomUserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    public AuthService(AuthenticationManager authManager,
                       CustomUserDetailsService userDetailsService,
                       TokenService tokenService,
                       JwtProperties jwtProperties,
                       RefreshTokenRepository refreshTokenRepository,
                       UserService userService) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userService = userService;
    }

    public AuthenticationResponse authentication(AuthRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails user = userDetailsService.loadUserByUsername(request.email());
        User userOK = userService.getUserByEmail(user.getUsername());

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        long expirationTime = jwtProperties.refreshTokenExpiration();

        refreshTokenRepository.save(refreshToken, user);

        return new AuthenticationResponse(accessToken, refreshToken, expirationTime,
                userOK.getId(), userOK.getName());
    }

    public Optional<TokenPair> refreshAccessToken(String token) {
        Optional<UserDetails> stored = refreshTokenRepository.findUserDetailsByToken(token);
        if (stored.isEmpty()) {
            return Optional.empty();
        }
        UserDetails refreshTokenUserDetails = stored.get();;

        String extractedEmail;
        try {
            if (tokenService.isExpired(token)) {
                refreshTokenRepository.deleteByToken(token);
                return Optional.empty();
            }
            extractedEmail = tokenService.extractEmail(token);
        } catch (JwtException ex) {
            // token vencido o manipulado: JJWT tira excepción al parsearlo
            refreshTokenRepository.deleteByToken(token);
            return Optional.empty();
        }

        // se fija si el mail del token es el mismo que el del refresh guardado
        if (!Objects.equals(extractedEmail, refreshTokenUserDetails.getUsername())) {
            // Si alguien manipulo el token o no coincide, invalidamos por seguridad
            refreshTokenRepository.deleteByToken(token);
            return Optional.empty();
        }

        refreshTokenRepository.deleteByToken(token);

        String newAccessToken = generateAccessToken(refreshTokenUserDetails);
        String newRefreshToken = generateRefreshToken(refreshTokenUserDetails);

        refreshTokenRepository.save(newRefreshToken, refreshTokenUserDetails);

        return Optional.of(new TokenPair(newAccessToken, newRefreshToken));
    }

    public String generateTokenForUser(String email) {
        return generateAccessToken(userDetailsService.loadUserByUsername(email));
    }

    private String generateAccessToken(UserDetails user) {
        return tokenService.generate(user,
                new Date(System.currentTimeMillis() + jwtProperties.accessTokenExpiration()),
                Map.of(
                        "role",
                        user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                        "name", user.getUsername()
                ));
    }

    private String generateRefreshToken(UserDetails user) {
        return tokenService.generate(user,
                new Date(System.currentTimeMillis() + jwtProperties.refreshTokenExpiration()));
    }
}
