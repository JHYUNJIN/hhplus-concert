package kr.hhplus.be.server.infrastructure.persistence.concertDate.dto;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;

import java.sql.Timestamp;
import java.util.UUID;

public record ConcertDateWithSeatCountDto(
        String id, // DB로부터 받는 원시 String 타입
        String concertId, // DB로부터 받는 원시 String 타입
        Timestamp date,
        Timestamp deadline,
        Timestamp createdAt,
        Timestamp updatedAt,
        Long remainingSeatCount
) {
    // ConcertDate 객체로 변환하는 메서드
    public ConcertDate toDomainConcertDate() {
        if (this.remainingSeatCount == null || this.remainingSeatCount < 0) {
            throw new CustomException(ErrorCode.INVALID_SEAT_COUNT, "예약가능 좌석 수는 0 이상이어야 합니다.");
        }
        return ConcertDate.builder()
                .id(UUID.fromString(this.id))
                .concertId(UUID.fromString(this.concertId))
                .date(this.date != null ? this.date.toLocalDateTime() : null)
                .deadline(this.deadline != null ? this.deadline.toLocalDateTime() : null)
                .createdAt(this.createdAt != null ? this.createdAt.toLocalDateTime() : null)
                .updatedAt(this.updatedAt != null ? this.updatedAt.toLocalDateTime() : null)
                .remainingSeatCount(this.remainingSeatCount.intValue())
                .build();
    }
}