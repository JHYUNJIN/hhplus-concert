package kr.hhplus.be.server.user.port.out;

import kr.hhplus.be.server.user.domain.User;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID userId);

    boolean existsById(UUID userId);
    int chargePoint(UUID userId, BigDecimal amount);

    void deleteAll();
}
