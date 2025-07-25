package kr.hhplus.be.server.concert.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public interface RankingUpdateUseCase {
    void updateRankingIfNeeded(UUID concertDateId, LocalDateTime occurredAt);
}