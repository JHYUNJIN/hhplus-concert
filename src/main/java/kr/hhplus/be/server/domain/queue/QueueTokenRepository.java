package kr.hhplus.be.server.domain.queue;

import java.util.UUID;

public interface QueueTokenRepository {
    void save(QueueToken queueToken);
    String findTokenIdByUserIdAndConcertId(UUID userId, UUID concertId);
    QueueToken findQueueTokenByTokenId(String tokenId);
    Integer findWaitingPosition(QueueToken queueToken);
    Integer countWaitingTokens(UUID concertId);
    Integer countActiveTokens(UUID concertId);
    void expiresQueueToken(String tokenId);
}
