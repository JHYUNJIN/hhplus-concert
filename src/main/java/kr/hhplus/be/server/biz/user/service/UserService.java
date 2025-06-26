package kr.hhplus.be.server.biz.user.service;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true) // 조회 메서드에 readOnly 트랜잭션 적용
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 새로운 사용자를 등록하고 초기 금액을 설정합니다.
     * @param initialAmount 초기 보유 금액
     * @return 생성된 User 객체
     */
    @Transactional
    public User registerUser(BigDecimal initialAmount) {
        String userId = UUID.randomUUID().toString();
        // ID 중복 여부 확인은 UUID의 특성상 거의 불필요하지만, 로직상으로는 존재할 수 있음
        if (userRepository.existsById(userId)) {
            // 적절한 예외 처리: 예를 들어, UUID 생성 충돌 시 재시도 로직
            throw new IllegalStateException("Failed to generate unique user ID.");
        }
        User newUser = new User(userId, initialAmount);
        return userRepository.save(newUser);
    }

    /**
     * 특정 ID의 사용자를 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return 조회된 User 객체 (Optional)
     */
    public Optional<User> getUser(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * 모든 사용자를 조회합니다.
     * @return 모든 User 객체 리스트
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * 사용자의 잔액을 충전합니다. (비관적 락 사용)
     * @param userId 충전할 사용자의 ID
     * @param amount 충전할 금액
     * @return 업데이트된 User 객체
     * @throws IllegalArgumentException 사용자를 찾을 수 없거나 금액이 유효하지 않을 때
     */
    @Transactional(isolation = Isolation.READ_COMMITTED) // 동시성 제어를 위해 READ_COMMITTED 이상 사용 고려
    public User chargeBalance(String userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }

        // 비관적 락을 걸어 해당 사용자 레코드에 대한 동시 접근을 제어 (FOR UPDATE)
        // 참고) 비관적 락이란 데이터베이스에서 특정 레코드에 대해 다른 트랜잭션이 접근하지 못하도록 잠그는 방식입니다.
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.setAmount(user.getAmount().add(amount)); // 금액 충전
        return userRepository.save(user); // 변경된 금액 저장
    }

    /**
     * 사용자의 잔액을 사용합니다. (비관적 락 사용)
     * @param userId 금액을 사용할 사용자의 ID
     * @param amount 사용할 금액
     * @return 업데이트된 User 객체
     * @throws IllegalArgumentException 사용자를 찾을 수 없거나 금액이 부족할 때
     */
    @Transactional(isolation = Isolation.READ_COMMITTED) // 동시성 제어를 위해 READ_COMMITTED 이상 사용 고려
    public User useBalance(String userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }

        // 비관적 락을 걸어 해당 사용자 레코드에 대한 동시 접근을 제어 (FOR UPDATE)
        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        if (user.getAmount().compareTo(amount) < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다. 현재 잔액: " + user.getAmount() + ", 요청 금액: " + amount);
        }

        user.setAmount(user.getAmount().subtract(amount)); // 금액 사용
        return userRepository.save(user); // 변경된 금액 저장
    }
}