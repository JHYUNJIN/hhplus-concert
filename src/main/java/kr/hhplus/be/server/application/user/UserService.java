package kr.hhplus.be.server.application.user;

import kr.hhplus.be.server.application.user.dto.RegisterUserCommand;
import kr.hhplus.be.server.application.user.dto.UserCommand;
import kr.hhplus.be.server.application.user.dto.UserQueryResult;
import kr.hhplus.be.server.common.exception.domain.UserException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
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
     * 사용자 ID는 커맨드에서 제공되며, 초기 금액도 설정됩니다.
     * @param command 사용자 등록 커맨드 (userId, initialAmount 포함)
     * @return 생성된 UserQueryResult 객체
     * @throws UserException 사용자 ID가 이미 존재할 경우 (ErrorCode.BAD_REQUEST)
     */
    @Transactional
    public UserQueryResult registerUser(RegisterUserCommand command) { // 파라미터 이름을 'command'로 변경
        // 1. 커맨드로부터 받은 userId 사용 (UUID.randomUUID() 생성 로직 제거)
        String userId = command.getUserId();

        // 2. 사용자 ID 중복 여부 확인
        if (userRepository.existsById(userId)) {
            throw new UserException(ErrorCode.BAD_REQUEST, "이미 존재하는 사용자 ID입니다: " + userId);
        }

        // 3. 새로운 User 엔티티 생성 (command에서 받은 userId와 initialAmount 사용)
        User newUser = new User(userId, command.getInitialAmount());

        // 4. User 엔티티 저장
        User savedUser = userRepository.save(newUser);

        // 5. UserQueryResult로 변환하여 반환
        return UserQueryResult.from(savedUser);
    }


    /**
     * 특정 ID의 사용자를 조회합니다.
//     * @param userId 조회할 사용자의 ID
     * @return 조회된 User 객체
     * @throws UserException 사용자를 찾을 수 없을 때 (ErrorCode.USER_NOT_FOUND)
     */
    public UserQueryResult getUser(UserCommand userCommand) {
        // userRepository.findById는 Optional<User>를 반환합니다.
        // Optional이 비어있으면 UserException을 발생시킵니다.
        User user = userRepository.findById(userCommand.getUserId())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + userCommand.getUserId()));

        // 조회된 User 엔티티를 UserQueryResult DTO로 변환하여 반환합니다.
        return UserQueryResult.from(user);
    }

    /**
     * 모든 사용자를 조회합니다.
     * @return 모든 User 객체 리스트
     */
    public List<UserQueryResult> getAllUsers() {
         return userRepository.findAll().stream()
                .map(UserQueryResult::from) // User 엔티티를 UserQueryResult DTO로 변환
                .toList(); // List<UserQueryResult>로 변환하여 반환
    }



    /**
     * 사용자의 잔액을 충전합니다. (비관적 락 사용)
     * @param userId 충전할 사용자의 ID
     * @param amount 충전할 금액
     * @return 업데이트된 User 객체
     * @throws UserException 사용자를 찾을 수 없거나 금액이 유효하지 않을 때
     */
    @Transactional(isolation = Isolation.READ_COMMITTED) // 동시성 제어를 위해 READ_COMMITTED 이상 사용 고려
    // 비관적 락을 걸어 해당 사용자 레코드에 대한 동시 접근을 제어 (FOR UPDATE)
    // 참고) 비관적 락이란 데이터베이스에서 특정 레코드에 대해 다른 트랜잭션이 접근하지 못하도록 잠그는 방식입니다.
    public UserQueryResult chargeBalance(String userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new UserException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND,  userId));

        user.setAmount(user.getAmount().add(amount));
        userRepository.save(user);
        return UserQueryResult.from(user);
    }

    /**
     * 사용자의 잔액을 사용합니다. (비관적 락 사용)
     * @param userId 금액을 사용할 사용자의 ID
     * @param amount 사용할 금액
     * @return 업데이트된 User 객체
     * @throws UserException 사용자를 찾을 수 없거나 잔액이 부족할 때
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public User useBalance(String userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new UserException(ErrorCode.INVALID_USE_AMOUNT);
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, userId));

        if (user.getAmount().compareTo(amount) < 0) {
            throw new UserException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        user.setAmount(user.getAmount().subtract(amount));
        return userRepository.save(user);
    }
}