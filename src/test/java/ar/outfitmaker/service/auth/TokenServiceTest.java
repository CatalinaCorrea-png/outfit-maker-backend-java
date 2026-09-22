package ar.outfitmaker.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ar.outfitmaker.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

class TokenServiceTest {

    private static final String KEY = "clave-de-test-con-al-menos-32-caracteres-ok";

    private TokenService tokenService;
    private UserDetails cher;

    @BeforeEach
    void setup() {
        tokenService = new TokenService(new JwtProperties(KEY, 900_000, 604_800_000));
        cher = User.withUsername("cher@gmail.com").password("irrelevante").build();
    }

    @Test
    void extractEmail_returnsTokensSubject() {
        String token = tokenService.generate(cher, enUnaHora());

        assertThat(tokenService.extractEmail(token)).isEqualTo("cher@gmail.com");
    }

    @Test
    void isValid_isTrueForTokenOwner() {
        String token = tokenService.generate(cher, enUnaHora());

        assertThat(tokenService.isValid(token, cher)).isTrue();
    }

    @Test
    void isValid_isFalseForOtherUser() {
        String token = tokenService.generate(cher, enUnaHora());
        UserDetails otherUser = User.withUsername("other@gmail.com").password("irrelevante").build();

        assertThat(tokenService.isValid(token, otherUser)).isFalse();
    }

    @Test
    void expiredToken_throwsExceptionWhenParsed() {
        String token = tokenService.generate(cher, haceUnMinuto());

        // JJWT no devuelve los claims de un token vencido: tira excepción.
        // Por eso AuthService.refreshAccessToken envuelve isExpired en un try/catch.
        assertThatThrownBy(() -> tokenService.isExpired(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithOtherKey_throwsException() {
        TokenService otroServidor = new TokenService(
                new JwtProperties("otra-clave-distinta-tambien-de-32-caracteres", 900_000, 604_800_000)
        );
        String tokenAjeno = otroServidor.generate(cher, enUnaHora());

        assertThatThrownBy(() -> tokenService.extractEmail(tokenAjeno))
                .isInstanceOf(JwtException.class);
    }

    private static Date enUnaHora() {
        return new Date(System.currentTimeMillis() + 3_600_000);
    }

    private static Date haceUnMinuto() {
        return new Date(System.currentTimeMillis() - 60_000);
    }
}
