package kr.hhplus.be.server.application.user.dto;

import lombok.*;

// User 관련 일반적인 명령을 담는 DTO
// 특정 유즈케이스(예: 충전, 사용)에 특화된 Command DTO들과는 다릅니다.
// 이 Command는 대상 사용자의 ID를 주로 포함합니다.
@Getter
@ToString
@RequiredArgsConstructor
public class UserCommand {
    private final String userId;


    // 만약 이 UserCommand가 여러 가지 일반적인 사용자 작업을 포괄한다면,
    // 여기에 작업 유형을 나타내는 필드나 다른 일반적인 필드들을 추가할 수 있습니다.
    // 예: private String operationType;
    // 예: private String newEmail; // 사용자 정보 업데이트 시 사용될 수 있음
}