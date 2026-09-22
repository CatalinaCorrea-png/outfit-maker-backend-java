package ar.outfitmaker.controller.auth;

import ar.outfitmaker.config.JwtProperties;
import ar.outfitmaker.domain.User;
import ar.outfitmaker.dto.auth.*;
import ar.outfitmaker.service.UserService;
import ar.outfitmaker.service.auth.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtProperties jwtProperties;

    public AuthController(UserService userService, AuthService authService, JwtProperties jwtProperties) {
        this.userService = userService;
        this.authService = authService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping
    public AuthenticationResponse authenticate(
            @RequestBody AuthRequest authRequest,
            HttpServletResponse response // esto no lo manda el front, es para armar vos la respuesta
            ) {
        AuthenticationResponse authResponse = authService.authentication(authRequest);

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(authResponse.refreshToken()).toString());

        return authResponse.withoutRefreshToken();
    }

    @PostMapping("/refresh")
    public TokenResponse refreshAccessToken(
            // cuando no viene la cookie Spring responde 400 y el if nunca se ejecuta. Queremos 401, no 400.
            @CookieValue(name = "refreshToken", required = false) String token,
            HttpServletResponse response
    ) {
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        TokenPair tokens = authService.refreshAccessToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(tokens.refreshToken()).toString());

        return new TokenResponse(tokens.accessToken());
    }

    @PostMapping("/register")
    public AuthResponse createUser(@RequestBody AuthRegisterRequest request) {
        User user = new User(request.email(), request.name(), "", request.password());
        User savedUser = userService.create(user);

        return new AuthResponse(savedUser.getName(), savedUser.getEmail(), savedUser.getId());
    }

    private ResponseCookie refreshCookie(String value) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(false) // todo: esto se tiene que cambiar, ahora localhost no es secure
                .path("/")
                .maxAge(Duration.ofMillis(jwtProperties.refreshTokenExpiration()))
                .sameSite("Strict")
                .build();
    }
}
