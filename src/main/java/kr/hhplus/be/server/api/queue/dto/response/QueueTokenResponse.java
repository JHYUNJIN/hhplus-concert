package kr.hhplus.be.server.api.queue.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.domain.queue.QueueStatus;
import kr.hhplus.be.server.domain.queue.QueueToken;
import lombok.Builder;

@Builder
public record QueueTokenResponse(
        @Schema(description = "발급된 대기열 토큰 ID")
        UUID tokenId,
        @Schema(description = "유저 ID")
        UUID userId,
        @Schema(description = "콘서트 ID")
        UUID concertId,
        @Schema(description = "토큰 상태")
        QueueStatus status,
        @Schema(description = "현재 순서")
        Integer position,
        @Schema(description = "토큰 발급 시간")
        LocalDateTime issuedAt,
        @Schema(description = "토큰 만료 시간")
        LocalDateTime expiresAt,
        @Schema(description = "활성된 시간")
        LocalDateTime enteredAt,
        @Schema(description = "예상 대기 시간(분)")
        Integer waitTime
) {
    public static QueueTokenResponse from(QueueToken queueToken) {
        return QueueTokenResponse.builder()
                .tokenId(queueToken.tokenId())
                .userId(queueToken.userId())
                .concertId(queueToken.concertId())
                .status(queueToken.status())
                .position(queueToken.position())
                .issuedAt(queueToken.issuedAt())
                .expiresAt(queueToken.expiresAt())
                .enteredAt(queueToken.enteredAt())
                .waitTime(queueToken.position() * 3) 	// 대기 순서당 3분 예상
                .build();
    }
}
