package kr.hhplus.be.server.infrastructure.web.user.dto;

import kr.hhplus.be.server.domain.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWebResponse {
    private String userId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static UserWebResponse from(User user) {
        return new UserWebResponse(user.getId(), user.getAmount(), user.getCreatedAt(), user.getUpdatedAt());
    }
}