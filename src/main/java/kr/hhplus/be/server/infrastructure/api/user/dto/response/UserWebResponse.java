package kr.hhplus.be.server.infrastructure.api.user.dto.response;

import kr.hhplus.be.server.application.user.dto.UserQueryResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserWebResponse {
    private final String userId;
    private final BigDecimal amount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static UserWebResponse from(UserQueryResult userQueryResult) {
        return new UserWebResponse(
                userQueryResult.getUserId(),
                userQueryResult.getAmount(),
                userQueryResult.getCreatedAt(),
                userQueryResult.getUpdatedAt()
        );
    }
}
