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
