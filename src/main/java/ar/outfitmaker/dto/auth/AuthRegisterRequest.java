package ar.outfitmaker.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Las mismas reglas que valida el front (User.validateRegister).
 */
public record AuthRegisterRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @NotBlank
        @Email
        String email,

        // BCrypt solo usa los primeros 72 bytes: más largo se truncaría en silencio
        @NotBlank
        @Size(min = 8, max = 72)
        String password
) {
}
