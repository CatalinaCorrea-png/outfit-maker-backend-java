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
        cher = User.withUsername("cher@gmail.com").password("irrelevant").build();
    }

    @Test
    void extractEmail_returnsTokenSubject() {
        String token = tokenService.generate(cher, inOneHour());

        assertThat(tokenService.extractEmail(token)).isEqualTo("cher@gmail.com");
    }

    @Test
    void isValid_isTrueForTokenOwner() {
        String token = tokenService.generate(cher, inOneHour());

        assertThat(tokenService.isValid(token, cher)).isTrue();
    }

    @Test
    void isValid_isFalseForOtherUser() {
        String token = tokenService.generate(cher, inOneHour());
        UserDetails otherUser = User.withUsername("other@gmail.com").password("irrelevant").build();

        assertThat(tokenService.isValid(token, otherUser)).isFalse();
    }

    @Test
    void isExpired_throwsForExpiredToken() {
        String token = tokenService.generate(cher, oneMinuteAgo());

        // JJWT no devuelve los claims de un token vencido: tira excepción.
        // Por eso AuthService.refreshAccessToken envuelve isExpired en un try/catch.
        assertThatThrownBy(() -> tokenService.isExpired(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractEmail_rejectsTokenSignedWithOtherKey() {
        TokenService otherServer = new TokenService(
                new JwtProperties("otra-clave-distinta-tambien-de-32-caracteres", 900_000, 604_800_000)
        );
        String foreignToken = otherServer.generate(cher, inOneHour());

        assertThatThrownBy(() -> tokenService.extractEmail(foreignToken))
                .isInstanceOf(JwtException.class);
    }

    private static Date inOneHour() {
        return new Date(System.currentTimeMillis() + 3_600_000);
    }

    private static Date oneMinuteAgo() {
        return new Date(System.currentTimeMillis() - 60_000);
    }
}
