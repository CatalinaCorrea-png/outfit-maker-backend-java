package ar.outfitmaker.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ar.outfitmaker.config.JwtProperties;
import ar.outfitmaker.domain.User;
import ar.outfitmaker.dto.auth.AuthRequest;
import ar.outfitmaker.dto.auth.AuthenticationResponse;
import ar.outfitmaker.dto.auth.TokenPair;
import ar.outfitmaker.repository.RefreshTokenRepository;
import ar.outfitmaker.service.CustomUserDetailsService;
import ar.outfitmaker.service.UserService;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final JwtProperties PROPERTIES = new JwtProperties("clave-de-test-con-al-menos-32-caracteres-ok", 900_000, 604_800_000);

    // Mocks: detrás tienen Spring Security o la base de datos
    @Mock
    private AuthenticationManager authManager;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private UserService userService;

    // Reales: lógica pura, baratos de construir
    private TokenService tokenService;
    private RefreshTokenRepository refreshTokenRepository;

    private AuthService authService;

    private final UserDetails cherDetails = userDetails("cher@gmail.com");
    private final UserDetails dionneDetails = userDetails("dionne@gmail.com");

    @BeforeEach
    void setup() {
        tokenService = new TokenService(PROPERTIES);
        refreshTokenRepository = new RefreshTokenRepository();
        authService = new AuthService(authManager, userDetailsService, tokenService,
                PROPERTIES, refreshTokenRepository, userService);
    }

    // ─── authentication ─────────────────────────────────────────────────────

    @Test
    void authentication_propagatesBadCredentials_andIssuesNoTokens() {
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.authentication(new AuthRequest("cher@gmail.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(userDetailsService, userService);
    }

    @Test
    void authentication_returnsUserData_andStoresRefreshToken() {
        User cher = new User("cher@gmail.com", "Cher", "", "hash");
        cher.setId("user-1");

        when(userDetailsService.loadUserByUsername("cher@gmail.com")).thenReturn(cherDetails);
        when(userService.getUserByEmail("cher@gmail.com")).thenReturn(cher);

        AuthenticationResponse response = authService.authentication(new AuthRequest("cher@gmail.com", "123"));
        assertThat(response.id()).isEqualTo("user-1");
        assertThat(response.name()).isEqualTo("Cher");
        assertThat(tokenService.extractEmail(response.accessToken())).isEqualTo("cher@gmail.com");
        assertThat(refreshTokenRepository.findUserDetailsByToken(response.refreshToken())).contains(cherDetails);
    }

    // ─── refreshAccessToken ─────────────────────────────────────────────────

    @Test
    void refreshAccessToken_returnsEmptyForUnknownToken() {
        String neverStored = tokenService.generate(cherDetails, inOneHour());

        assertThat(authService.refreshAccessToken(neverStored)).isEmpty();
    }

    @Test
    void refreshToken_rotatesTokens() {
        String oldToken = storedRefreshToken(cherDetails, inOneHour());

        Optional<TokenPair> result = authService.refreshAccessToken(oldToken);

        assertThat(result).isPresent();
        TokenPair tokens = result.get();
        assertThat(tokenService.extractEmail(tokens.accessToken())).isEqualTo("cher@gmail.com");
        assertThat(refreshTokenRepository.findUserDetailsByToken(tokens.refreshToken())).contains(cherDetails);
        assertThat(refreshTokenRepository.findUserDetailsByToken(oldToken)).isEmpty();
    }

    @Test
    void refreshAccessToken_returnsEmptyAndDeletesExpiredToken() {
        String expired = storedRefreshToken(cherDetails, oneMinuteAgo());

        assertThat(authService.refreshAccessToken(expired)).isEmpty();
        assertThat(refreshTokenRepository.findUserDetailsByToken(expired)).isEmpty();
    }

    @Test
    void refreshAccessToken_rejectsTokenStoredForAnotherUser() {
        String cherToken = tokenService.generate(cherDetails, inOneHour());
        refreshTokenRepository.save(cherToken, dionneDetails);

        assertThat(authService.refreshAccessToken(cherToken)).isEmpty();
        assertThat(refreshTokenRepository.findUserDetailsByToken(cherToken)).isEmpty();
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private String storedRefreshToken(UserDetails owner, Date expiration) {
        String token = tokenService.generate(owner, expiration);
        refreshTokenRepository.save(token, owner);
        return token;
    }

    private static UserDetails userDetails(String email) {
        return org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("hash")
                .build();
    }

    private static Date inOneHour() {
        return new Date(System.currentTimeMillis() + 3_600_000);
    }

    private static Date oneMinuteAgo() {
        return new Date(System.currentTimeMillis() - 60_000);
    }
}
