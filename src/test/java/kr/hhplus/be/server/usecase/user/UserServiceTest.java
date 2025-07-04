package kr.hhplus.be.server.usecase.user;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class) // Mockito를 활용한 유닛테스트
public class UserServiceTest {

    /*
    @Mock은 진짜 객체 대신 동작을 흉내내는 가짜(Mock) 객체를 생성합니다.
    @InjectMocks는 테스트 대상 객체(실제 객체)를 만들고, 그 안에 필요한 의존성(Mock 객체 등)을 주입합니다.
    즉,
    @Mock: 가짜 객체(동작만 흉내냄, 실제 로직 없음)
    @InjectMocks: 실제 객체(테스트 대상), 단 의존성은 가짜(Mock)로 대체됨
    테스트에서 진짜 객체의 동작을 검증하되, 의존성은 가짜로 대체해 외부 영향 없이 테스트할 수 있습니다.
     */
    @InjectMocks // UserService의 Mock 객체 생성
    private UserService userService;

    @Mock // UserRepository의 Mock 객체 생성
    private UserRepository userRepository;

    // 테스트에 사용할 유저 정보
    private UUID userId;
    private BigDecimal initAmount;
    private User user;

    @BeforeEach // 테스트 실행 전 항상 유저 정보 초기화
    void beforeEach() {
        userId = UUID.randomUUID();
        initAmount = BigDecimal.valueOf(20000);
        user = User.builder()
                .id(userId)
                .amount(initAmount)
                .build();
    }


    @Test
    @DisplayName("유저_조회_성공")
    void getUser_Success() throws CustomException {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User findUser = userService.getUser(userId);

        verify(userRepository, times(1)).findById(userId);
        assertThat(findUser).isNotNull();
        assertThat(findUser.id()).isEqualTo(userId);
        assertThat(findUser.amount()).isEqualTo(initAmount);
    }

    @Test
    @DisplayName("유저_조회_실패")
    void getUser_Failure_UserNotFound() throws CustomException {
        // userRepository.findById(userId)를 호출했을 때, Optional.empty()를 반환하도록 설정
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CustomException customException = assertThrows(CustomException.class,
                () -> userService.getUser(userId));

        verify(userRepository, times(1)).findById(userId);
        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("유저_포인트_충전_성공")
    void chargePoint_Success() throws CustomException {
        BigDecimal chargePoint = BigDecimal.valueOf(5000);
        User chargedUser = user.charge(chargePoint); // 유저의 포인트를 충전한 새로운 User 객체 생성

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(chargedUser); // save 메서드가 호출되면 충전된 유저 객체를 반환하도록 설정

        User user = userService.chargePoint(userId, chargePoint);

        verify(userRepository, times(1)).findById(userId); // 유저 조회 메서드가 1번 호출되었는지 확인

        assertThat(user).isNotNull(); // 유저 정보가 null이 아님을 확인
        assertThat(user.amount()).isEqualTo(initAmount.add(chargePoint)); // 유저의 포인트가 충전된 금액만큼 증가했는지 확인
    }

    @Test
    @DisplayName("유저_포인트_충전_실패_유저못찾음")
    void chargePoint_Failure_UserNotFound() throws CustomException {
        BigDecimal chargePoint = BigDecimal.valueOf(5000);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CustomException customException = assertThrows(CustomException.class,
                () -> userService.chargePoint(userId, chargePoint));

        verify(userRepository, times(1)).findById(userId);

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("유저_포인트_충전_실패_최소충전금액미만(1000원)")
    void chargePoint_Failure_NotEnoughMinChargePoint() {
        BigDecimal chargePoint = BigDecimal.valueOf(500);

        CustomException customException = assertThrows(CustomException.class,
                () -> userService.chargePoint(userId, chargePoint));

        verify(userRepository, never()).findById(userId); // 최소 충전 금액 미만일 때, 유저 조회 안함을 확인
        // never() : findById(userId)가 호출되지 않았는지 검증

        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.NOT_ENOUGH_MIN_CHARGE_POINT);
    }

}
