package kr.hhplus.be.server.queue.domain;

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
    public static QueueToken waitingTokenOf(UUID tokenId, UUID userId, UUID concertId, int waitingTokens, long expiresIn) {
        LocalDateTime now = LocalDateTime.now();
        return QueueToken.builder()
                .tokenId(tokenId)
                .userId(userId)
                .concertId(concertId)
                .status(QueueStatus.WAITING)
                .position(waitingTokens + 1)
                .issuedAt(now)
                .enteredAt(null)
                .expiresAt(now.plusSeconds(expiresIn))
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
                .issuedAt(issuedAt) // Keep original issuedAt
                .enteredAt(enteredAt) // Keep original enteredAt
                .expiresAt(expiresAt) // Keep original expiresAt
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
