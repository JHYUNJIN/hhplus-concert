package kr.hhplus.be.server.domain.queue;

import kr.hhplus.be.server.domain.concert.Concert;

import java.util.List;
import java.util.UUID;

public interface QueueTokenRepository {
    void save(QueueToken queueToken);
    String findTokenIdByUserIdAndConcertId(UUID userId, UUID concertId);
    QueueToken findQueueTokenByTokenId(String tokenId);
    Integer findWaitingPosition(QueueToken queueToken);
    Integer countWaitingTokens(UUID concertId);
    Integer countActiveTokens(UUID concertId);
    void expiresQueueToken(String tokenId);
    void promoteQueueToken(List<Concert> openConcerts);
}
