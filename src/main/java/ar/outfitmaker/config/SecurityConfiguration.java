package ar.outfitmaker.config;

import ar.outfitmaker.service.CustomUserDetailsService;
import ar.outfitmaker.service.auth.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@Profile("!test")
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;

    public SecurityConfiguration(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(CustomUserDetailsService userDetailsService,
                                                           TokenService tokenService,
                                                           ObjectMapper objectMapper) {
        return new JwtAuthenticationFilter(userDetailsService, tokenService, objectMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        return http
                .cors(Customizer.withDefaults())
                // Access token viaja en header Authorization (no cookie), inmune a CSRF.
                // Refresh token en cookie SameSite=Strict, el browser no la manda cross-site.
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publicos
                        .requestMatchers("/auth", "/auth/refresh", "/error").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // react pregunta antes de hacer la request real
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        // el resto esta bloqueado si no se autentica
                        .anyRequest().fullyAuthenticated())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // manda 401 si no tenes token o es invalido
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                ))
                .build();
    }

    @Bean // Spring Security (antes de los filtros de seguridad)
    public CorsConfigurationSource corsConfigurationSource(
            // Orígenes permitidos por CORS, separados por coma. Local: Vite dev (5173).
            // En la nube se agrega la URL del front con la env var CORS_ALLOWED_ORIGINS.
            @Value("${cors.allowed-origins:http://localhost:5173}") String allowedOriginsCsv) {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim)
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("WWW-Authenticate"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // esto cachea el options

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
