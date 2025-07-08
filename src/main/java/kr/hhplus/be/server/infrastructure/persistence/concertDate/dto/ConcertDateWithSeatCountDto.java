package kr.hhplus.be.server.infrastructure.persistence.concertDate.dto; // 적절한 패키지 경로

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;

import java.time.LocalDateTime;

public record ConcertDateWithSeatCountDto(
        String id,
        String concertId,
        LocalDateTime date,
        LocalDateTime deadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long remainingSeatCount // 'count'는 Long 타입이 적절한 이유 : 좌석 수는 음수일 수 없고, 최대값이 Integer.MAX_VALUE를 초과할 수 있기 때문
) {


     public ConcertDateWithSeatCountDto {
         if (remainingSeatCount < 0) {
             throw new CustomException(ErrorCode.INVALID_SEAT_COUNT, "예약가능 좌석 수는 음수일 수 없습니다.");
         }
     }
}