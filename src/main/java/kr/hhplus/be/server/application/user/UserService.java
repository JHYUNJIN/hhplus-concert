package kr.hhplus.be.server.application.user;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private static final BigDecimal MIN_CHARGE_POINT = BigDecimal.valueOf(1000);

    private final UserRepository userRepository;

    // 유저 생성
    @Transactional
    public User createUser(User user) throws CustomException {
        if (user == null) {
            log.warn("유저 생성 실패 - 유저 정보가 유효하지 않음");
            throw new CustomException(ErrorCode.INVALID_USER_DATA);
        }

        User savedUser = userRepository.save(user);
        log.info("유저 생성: USER_ID - {}, INITIAL_POINT - {}", savedUser.id(), savedUser.amount());
        return savedUser;
    }

    public User getUser(UUID userId) throws CustomException {
        return findUser(userId);
    }

    @Transactional
    public User chargePoint(UUID userId, BigDecimal point) throws CustomException {
        if (point.compareTo(MIN_CHARGE_POINT) < 0) {
            log.warn("유저 포인트 충전 실패 - 최소 충전 금액 미만: USER_ID - {}, CHARGE_POINT - {}", userId, point);
            throw new CustomException(ErrorCode.NOT_ENOUGH_MIN_CHARGE_POINT);
        }

        User user = findUser(userId);

        User charged = user.charge(point);
        User saved = userRepository.save(charged);

        log.info("유저 포인트 충전: USER_ID - {}, CHARGE_POINT - {}, AFTER_POINT - {}", userId, point, user.amount());
        return saved;
    }

    private User findUser(UUID userId) throws CustomException {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            log.debug("유저 조회: USER_ID - {}", userId);
            return user;
        } catch (CustomException e) {
            log.warn("유저 조회 실패: USER_ID - {}", userId);
            throw e;
        }
    }
}
