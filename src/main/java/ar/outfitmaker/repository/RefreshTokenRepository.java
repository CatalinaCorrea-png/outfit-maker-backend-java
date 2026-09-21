package ar.outfitmaker.repository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RefreshTokenRepository {
    /**  ConcurrentHashMap: Este bean es único y lo tocan varios requests al mismo tiempo. */
    private final Map<String, UserDetails> tokens = new ConcurrentHashMap<>();

    public void save(String token, UserDetails userDetails) {
        tokens.put(token, userDetails);
    }

    public Optional<UserDetails> findUserDetailsByToken(String token) {
        return Optional.ofNullable(tokens.get(token));
    }

    public void deleteByToken(String token) {
        tokens.remove(token);
    }
}
