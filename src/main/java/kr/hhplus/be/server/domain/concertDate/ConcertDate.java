package kr.hhplus.be.server.domain.concertDate;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ConcertDate(
        UUID id,
        UUID concertId,
        Integer remainingSeatCount, // 현재 남은 좌석 수
        LocalDateTime date,
        LocalDateTime deadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /**
     * JPQL DTO 프로젝션을 위한 생성자. (findAvailableDatesWithAvailableSeatCount)
     * @Query 내에서 new 키워드를 사용하면, DB에서 조회된 원시 타입(예: String, Long)을
     * 그대로 전달받을 생성자가 필요합니다. 이 생성자는 해당 값들을 애플리케이션의
     * 도메인 타입(UUID, Integer)으로 변환하는 역할을 수행합니다.
     * 이는 DB에 부담을 주지 않고, 코드의 이식성과 유지보수성을 높이는 방법입니다.
     */
    public ConcertDate(String id, String concertId, Long remainingSeatCount, LocalDateTime date, LocalDateTime deadline, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(
                UUID.fromString(id), // String -> UUID
                UUID.fromString(concertId), // String -> UUID
                remainingSeatCount.intValue(), // Long -> Integer
                date,
                deadline,
                createdAt,
                updatedAt
        );
    }

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