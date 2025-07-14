package kr.hhplus.be.server.infrastructure.persistence.user;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserJpaGateway implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        UserEntity userEntity = UserEntity.from(user);
        return jpaUserRepository.save(userEntity).toDomain();
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return jpaUserRepository.findById(userId.toString())
                .map(UserEntity::toDomain);
    }

    @Override
    public boolean existsById(UUID userId) {
        return jpaUserRepository.existsById(userId.toString());
    }

    @Override
    public int chargePoint(UUID userId, BigDecimal amount) {
        return jpaUserRepository.chargePoint(userId.toString(), amount);
    }

    @Override
    public void deleteAll() {
        jpaUserRepository.deleteAll();
    }


}
