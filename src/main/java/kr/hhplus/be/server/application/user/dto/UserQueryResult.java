package kr.hhplus.be.server.application.user.dto;

import kr.hhplus.be.server.domain.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@ToString
public class UserQueryResult {
    private final String userId;
    private final BigDecimal amount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static UserQueryResult from(User user) {
        return new UserQueryResult(
                user.getId(),
                user.getAmount(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
