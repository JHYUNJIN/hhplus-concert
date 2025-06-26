package kr.hhplus.be.server.biz.user.dto;

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
public class UserResponse {
    private String userId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getAmount(), user.getCreatedAt(), user.getUpdatedAt());
    }
}