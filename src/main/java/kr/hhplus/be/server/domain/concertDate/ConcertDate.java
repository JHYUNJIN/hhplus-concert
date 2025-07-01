package kr.hhplus.be.server.domain.concertDate;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ConcertDate(
        UUID id,
        UUID concertId,
        Integer remainingSeatCount,
        LocalDateTime date,
        LocalDateTime deadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public boolean checkDeadline() {
        return deadline.isAfter(LocalDateTime.now());
    }

    public ConcertDate withRemainingSeatCount(Integer availableSeatCount) {
        return ConcertDate.builder()
                .id(id)
                .concertId(concertId)
                .remainingSeatCount(availableSeatCount)
                .date(date)
                .deadline(deadline)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
