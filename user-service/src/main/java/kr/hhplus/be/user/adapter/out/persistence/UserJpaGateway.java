package kr.hhplus.be.user.adapter.out.persistence;

import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.user.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserJpaGateway implements UserRepository {

    private final kr.hhplus.be.user.adapter.out.persistence.JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        kr.hhplus.be.user.adapter.out.persistence.UserEntity userEntity = kr.hhplus.be.user.adapter.out.persistence.UserEntity.from(user);
        return jpaUserRepository.save(userEntity).toDomain();
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return jpaUserRepository.findById(userId.toString())
                .map(kr.hhplus.be.user.adapter.out.persistence.UserEntity::toDomain);
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
