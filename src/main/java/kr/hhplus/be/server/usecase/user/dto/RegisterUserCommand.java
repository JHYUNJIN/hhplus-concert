package kr.hhplus.be.server.usecase.user.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 새로운 사용자를 등록하기 위한 커맨드 (유즈케이스의 입력)
 */
@Getter
@RequiredArgsConstructor // final 필드만을 가지는 생성자를 자동으로 생성합니다.
@ToString
public class RegisterUserCommand {
    private final String userId;
    private final BigDecimal initialAmount;
}
