package kr.hhplus.be.server.domain.rank;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record SoldOutRank(
        UUID id,
        UUID concertId,
        long score,
        long soldOutTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static SoldOutRank of(UUID concertId, long score, long soldOutTime) {
        return SoldOutRank.builder()
                .concertId(concertId)
                .score(score)
                .soldOutTime(soldOutTime)
                .build();
    }
}
