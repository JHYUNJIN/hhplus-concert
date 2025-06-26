package kr.hhplus.be.server.infrastructure.persistence.user; // 해당 기능 모듈의 repository 폴더

import kr.hhplus.be.server.domain.user.User; // User 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.Optional;

// Spring Data JPA에서 자동으로 구현해줄 인터페이스
public interface UserJpaRepository extends JpaRepository<User, String> { // User 엔티티와 ID 타입 (String) 명시
    // findByIdForUpdate를 위한 비관적 락 쿼리 (UUID는 String으로 변환하여 사용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdWithPessimisticLock(String id);
}