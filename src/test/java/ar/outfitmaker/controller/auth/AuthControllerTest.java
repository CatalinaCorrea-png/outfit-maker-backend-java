package ar.outfitmaker.controller.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.outfitmaker.config.JwtProperties;
import ar.outfitmaker.domain.User;
import ar.outfitmaker.dto.auth.AuthenticationResponse;
import ar.outfitmaker.errors.ConflictException;
import ar.outfitmaker.service.UserService;
import ar.outfitmaker.service.auth.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @TestConfiguration
    static class JwtPropertiesTestConfig {
        @Bean
        JwtProperties jwtProperties() {
            return new JwtProperties("clave-de-test-con-al-menos-32-caracteres-ok",
                    900_000, 604_800_000);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserService userService;

    @Test
    void register_returnsThePublicDataOfTheCreatedUser() throws Exception {
        User saved = new User("test@test.com", "Test", "", "encoded");
        saved.setId("user-1");
        when(userService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test","email":"test@test.com","password":"12345678"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_returnsConflictWhenTheEmailIsTaken() throws Exception {
        when(userService.create(any()))
                .thenThrow(new ConflictException("USER_EMAIL_ALREADY_EXISTS", "Email ya registrado"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test","email":"test@test.com","password":"12345678"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void login_returnsAccessTokenInBody_andRefreshTokenOnlyInTheCookie() throws Exception {
        when(authService.authentication(any()))
                .thenReturn(new AuthenticationResponse("access-token", "refresh-token",
                        604_800_000L, "user-1", "Cher"));

        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"cher@gmail.com","password":"123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value(""))
                .andExpect(jsonPath("$.name").value("Cher"))
                .andExpect(cookie().value("refreshToken", "refresh-token"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }
    @Test
    void malformedJson_returnsRequestMalformed() throws Exception {
        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cher@gmail.com\",}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_MALFORMED"));
    }

    @Test
    void invalidEnumValueInBody_namesTheOffendingField() throws Exception {
        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":{"nested":"object"},"password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_MALFORMED"));
    }

    @Test
    void refreshWithoutCookie_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_setsRefreshCookieWithTheSameLifetimeAsTheToken() throws Exception {
        when(authService.authentication(any()))
                .thenReturn(new AuthenticationResponse("access-token", "refresh-token",
                        604_800_000L, "user-1", "Cher"));

        mockMvc.perform(post("/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"cher@gmail.com","password":"123"}
                                """))
                .andExpect(cookie().maxAge("refreshToken", 604_800));  // 7 días en segundos
    }
}