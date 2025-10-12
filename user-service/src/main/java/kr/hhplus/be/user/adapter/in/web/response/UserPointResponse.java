package kr.hhplus.be.server.user.adapter.in.web.response;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.user.domain.User;
import lombok.Builder;

@Builder
public record UserPointResponse(
        @Schema(description = "유저 ID")
        UUID userId,
        @Schema(description = "포인트 잔액")
        BigDecimal amount
) {

    public static UserPointResponse from(User user) {
        return UserPointResponse.builder()
                .userId(user.id())
                .amount(user.amount())
                .build();
    }
}
