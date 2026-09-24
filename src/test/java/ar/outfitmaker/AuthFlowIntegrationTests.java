package ar.outfitmaker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.outfitmaker.dto.auth.AuthenticationResponse;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthFlowIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullFlow_registerLoginUseProtectedEndpointAndRefresh() throws Exception {
        register("cher@gmail.com", "Cher", "12345678");

        // ── login ───────────────────────────────────────────────────────────
        MvcResult login = login("cher@gmail.com", "12345678")
                .andExpect(status().isOk())
                .andReturn();

        AuthenticationResponse tokens = objectMapper.readValue(
                login.getResponse().getContentAsString(), AuthenticationResponse.class
        );
        Cookie refreshCookie = login.getResponse().getCookie("refreshToken");

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isEmpty(); // solo viaja en la cookie
        assertThat(tokens.name()).isEqualTo("Cher");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.isHttpOnly()).isTrue();

        // ── endpoint protegido con el access token ──────────────────────────
        mockMvc.perform(get("/garments/filtered-garments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // ── refresh con la cookie ───────────────────────────────────────────
        MvcResult refreshed = mockMvc.perform(post("/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        Cookie rotatedCookie = refreshed.getResponse().getCookie("refreshToken");
        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(refreshCookie.getValue());

        // ── el refresh token viejo ya no sirve ──────────────────────────────
        mockMvc.perform(post("/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/garments/filtered-garments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withTamperedToken_returnsApiErrorFromTheFilter() throws Exception {
        register("dionne@gmail.com", "Dionne", "12345678");
        MvcResult login = login("dionne@gmail.com", "12345678").andReturn();
        AuthenticationResponse tokens = objectMapper.readValue(
                login.getResponse().getContentAsString(), AuthenticationResponse.class);

        String tampered = tokens.accessToken().substring(0, tokens.accessToken().length() - 4) + "aaaa";

        mockMvc.perform(get("/garments/filtered-garments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE))
                .andExpect(jsonPath("$.code").value("TOKEN_INVALID"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        register("cher@gmail.com", "Cher", "12345678");

        login("cher@gmail.com", "contraseña-incorrecta")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    void register_withDuplicateEmail_returnsConflict() throws Exception {
        register("cher@gmail.com", "Cher", "12345678");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("cher@gmail.com", "Otra Cher", "12345678")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USER_EMAIL_ALREADY_EXISTS"));
    }

    @Test
    void healthEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private void register(String email, String name, String password) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(email, name, password)))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions login(String email, String password)
            throws Exception {
        return mockMvc.perform(post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)));
    }

    private static String registerBody(String email, String name, String password) {
        return """
                {"name":"%s","email":"%s","password":"%s"}
                """.formatted(name, email, password);
    }
}
