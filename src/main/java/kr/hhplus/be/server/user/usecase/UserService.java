package kr.hhplus.be.server.user.usecase;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

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

        return userRepository.save(user);
    }

    public User getUser(UUID userId) throws CustomException {
        return findUser(userId);
    }

    @Transactional
    public User chargePoint(UUID userId, BigDecimal point) throws CustomException {
        // 1. 사전 검증 (락 없이)
        if (point.compareTo(MIN_CHARGE_POINT) < 0) {
            throw new CustomException(ErrorCode.NOT_ENOUGH_MIN_CHARGE_POINT);
        }

        // 2. 업데이트 (락 시간 최소화)
        int updatedRows = userRepository.chargePoint(userId, point);

        if (updatedRows == 0) {
            throw new CustomException(ErrorCode.CHARGE_FAILED);
        }

        // 3. 결과 반환
        return userRepository.findById(userId).orElseThrow();
    }

    private User findUser(UUID userId) throws CustomException {
        try {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        } catch (CustomException e) {
            log.warn("유저 조회 실패: USER_ID - {}", userId);
            throw e;
        }
    }
}
