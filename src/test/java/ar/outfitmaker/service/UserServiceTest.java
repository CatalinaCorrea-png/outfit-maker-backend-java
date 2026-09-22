package ar.outfitmaker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import ar.outfitmaker.domain.User;
import ar.outfitmaker.errors.ConflictException;
import ar.outfitmaker.errors.NotFoundException;
import ar.outfitmaker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    private UserService userService;

    @BeforeEach
    void setup() {
        userService = new UserService(userRepository, encoder);
    }

    @Test
    void create_throwsConflictWhenEmailAlreadyExists() {
        User existingUser = new User("cher@gmail.com", "Cher", "", "hash");
        when(userRepository.findByEmail("cher@gmail.com")).thenReturn(Optional.of(existingUser));

        User duplicate = new User("cher@gmail.com", "Another Cher", "", "123");

        assertThatThrownBy(() -> userService.create(duplicate))
                .isInstanceOfSatisfying(ConflictException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("USER_EMAIL_ALREADY_EXISTS"));
        // verify repo.save is not called with any parameter
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_storesEncodedPassword_neverThePlainOne() {
        when(userRepository.findByEmail("cher@gmail.com")).thenReturn(Optional.empty());
        when(encoder.encode("123")).thenReturn("encoded-123");

        userService.create(new User("cher@gmail.com", "Cher", "avatar.jpg", "123"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("encoded-123");
        assertThat(saved.getValue().getEmail()).isEqualTo("cher@gmail.com");
        assertThat(saved.getValue().getAvatarUrl()).isEqualTo("avatar.jpg");
    }

    @Test
    void getUserByEmail_throwsNotFoundWhenMissing() {
        String ghostEmail = "ghost@gmail.com";
        when(userRepository.findByEmail(ghostEmail)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail(ghostEmail))
                .isInstanceOfSatisfying(NotFoundException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("USER_NOT_FOUND"));
    }

}
