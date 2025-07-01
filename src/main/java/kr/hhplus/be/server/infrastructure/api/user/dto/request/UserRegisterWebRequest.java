// src/main/java/kr/hhplus/be/infrastructure/web/user/dto/request/UserRegisterWebRequest.java
package kr.hhplus.be.server.infrastructure.api.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 사용자 등록 요청 (Web Layer Input)
@Getter
@Setter
@NoArgsConstructor
@ToString
public class UserRegisterWebRequest {
    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId; // 클라이언트가 직접 넘길 사용자 ID

    // 초기 금액을 여기서 받을 수도 있습니다. (선택 사항)
    // private BigDecimal initialAmount;
}