package kr.hhplus.be.server.infrastructure.persistence.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 비관적 락 적용
    Optional<UserEntity> findById(String id);
}
