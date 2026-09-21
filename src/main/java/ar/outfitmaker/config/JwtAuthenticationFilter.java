package ar.outfitmaker.config;

import ar.outfitmaker.errors.ApiError;
import ar.outfitmaker.service.CustomUserDetailsService;
import ar.outfitmaker.service.auth.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String EXPIRED_CHALLENGE =
            "Bearer error=\"invalid_token\", error_description=\"The access token expired\"";
    private static final String INVALID_CHALLENGE =
            "Bearer error=\"invalid_token\", error_description=\"The access token is invalid\"";

    private final CustomUserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(CustomUserDetailsService userDetailsService,
                                   TokenService tokenService,
                                   ObjectMapper objectMapper) {
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        // Si no hay Bearer token, dejar pasar: Spring Security decide si el endpoint lo requiere
        if (doesNotContainBearerToken(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = authHeader.substring(BEARER_PREFIX.length());

        try {
            String email = tokenService.extractEmail(jwtToken);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails foundUser = userDetailsService.loadUserByUsername(email);

                if (tokenService.isValid(jwtToken, foundUser)) {
                    updateContext(foundUser, request);
                }
            }
        } catch (ExpiredJwtException ex) {
            writeUnauthorized(response, EXPIRED_CHALLENGE, "TOKEN_EXPIRED", "El token de acceso expiró");
            return;
        } catch (JwtException ex) {
            writeUnauthorized(response, INVALID_CHALLENGE, "TOKEN_INVALID", "Token inválido");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String wwwAuthenticate,
                                   String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiError.of(HttpServletResponse.SC_UNAUTHORIZED, code, "No autorizado", message)));
    }

    private void updateContext(UserDetails foundUser, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(foundUser, null, foundUser.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private boolean doesNotContainBearerToken(String authHeader) {
        return authHeader == null || !authHeader.startsWith(BEARER_PREFIX);
    }
}
