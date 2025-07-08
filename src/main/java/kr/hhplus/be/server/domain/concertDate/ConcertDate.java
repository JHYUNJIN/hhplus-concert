package kr.hhplus.be.server.domain.concertDate;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ConcertDate(
        UUID id,
        UUID concertId,
        Integer remainingSeatCount, // 현재 남은 좌석 수
        LocalDateTime date,
        LocalDateTime deadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public boolean checkDeadline() {
        return deadline.isAfter(LocalDateTime.now()); // 현재 시간보다 마감 시간이 이후인지 확인
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
