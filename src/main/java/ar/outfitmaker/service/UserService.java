package ar.outfitmaker.service;

import ar.outfitmaker.domain.User;
import ar.outfitmaker.errors.ConflictException;
import ar.outfitmaker.errors.NotFoundException;
import ar.outfitmaker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Transactional
    public User create(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ConflictException("USER_EMAIL_ALREADY_EXISTS",
                    "Email '" + user.getEmail() + "' ya se encuentra registrado");
        }

        User userCopy = new User(
                user.getEmail(),
                user.getName(),
                user.getAvatarUrl(),
                encoder.encode(user.getPassword())
        );
        userCopy.validate();
        return userRepository.save(userCopy);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND",
                        "No se encuentra un usuario registrado con este email: " + email));
    }
}
