package kr.hhplus.be.server.domain.user;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID userId);

    boolean existsById(UUID userId);
}
