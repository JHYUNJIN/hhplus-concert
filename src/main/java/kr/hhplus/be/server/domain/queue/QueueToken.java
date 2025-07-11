package kr.hhplus.be.server.domain.queue;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record QueueToken(
        UUID tokenId,
        UUID userId,
        UUID concertId,
        QueueStatus status,
        Integer position,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt,
        LocalDateTime enteredAt
) {

    // 활성 토큰을 생성하는 메서드
    public static QueueToken activeTokenOf(UUID tokenId, UUID userId, UUID concertId, long expiresTime) {
        LocalDateTime now = LocalDateTime.now();

        return QueueToken.builder()
                .tokenId(tokenId)
                .userId(userId)
                .concertId(concertId)
                .status(QueueStatus.ACTIVE)
                .position(0)
                .issuedAt(now)
                .enteredAt(now)
                .expiresAt(now.plusMinutes(expiresTime))
                .build();
    }

    // 대기 중인 토큰을 생성하는 메서드
    public static QueueToken waitingTokenOf(UUID tokenId, UUID userId, UUID concertId, int waitingTokens) {
        return QueueToken.builder()
                .tokenId(tokenId)
                .userId(userId)
                .concertId(concertId)
                .status(QueueStatus.WAITING)
                .position(waitingTokens + 1)
                .issuedAt(LocalDateTime.now())
                .enteredAt(null)
                .expiresAt(null)
                .build();
    }

    // 대기 중인 토큰의 순서를 업데이트하는 메서드
    public QueueToken withWaitingPosition(int waitingPosition) {
        return QueueToken.builder()
                .tokenId(tokenId)
                .userId(userId)
                .concertId(concertId)
                .status(status)
                .position(waitingPosition)
                .issuedAt(LocalDateTime.now())
                .enteredAt(null)
                .expiresAt(null)
                .build();
    }

    public boolean isActive() {
        return status.equals(QueueStatus.ACTIVE);
    }

    public boolean isExpired() {
        if (status.equals(QueueStatus.ACTIVE) && expiresAt != null) {
            return expiresAt.isBefore(LocalDateTime.now());
        }

        return false;
    }
}
