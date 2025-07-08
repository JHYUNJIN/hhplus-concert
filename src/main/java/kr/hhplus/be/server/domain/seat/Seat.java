package kr.hhplus.be.server.domain.seat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import lombok.Builder;

@Builder
public record Seat(
        UUID id,
        UUID concertDateId,
        int seatNo,
        BigDecimal price,
        SeatGrade seatGrade,
        SeatStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public boolean isAvailable() {
        return status.equals(SeatStatus.AVAILABLE);
    }

    public Seat reserve() throws CustomException { // 예외 처리를 위해 throws CustomException 추가
        /*
        * 고 튜터님 피드백 수정
        좌석 예약 로직과 유효성 검사 로직을 함께 캡슐화
        즉각적인 유효성 검사: 좌석 예약 요청이 들어왔을 때 유효성 검사를 즉시 수행하여, 불필요한 예약 시도나 오류를 줄일 수 있음
        코드 이해도 향상: reserve 함수만 보더라도 "이 함수는 예약하기 전에 좌석 사용 가능 여부를 먼저 확인하는구나"라고 직관적으로 이해 가능
         */
        // 1. 좌석 사용 가능 여부 확인
        if (!this.isAvailable())
            throw new CustomException(ErrorCode.ALREADY_RESERVED_SEAT);

        // 2. 좌석 예약 로직 수행
        return Seat.builder()
                .id(id)
                .concertDateId(concertDateId)
                .seatNo(seatNo)
                .price(price)
                .seatGrade(seatGrade)
                .status(SeatStatus.RESERVED) // 상태를 RESERVED로 변경
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Seat payment() {
        return Seat.builder()
                .id(id)
                .concertDateId(concertDateId)
                .seatNo(seatNo)
                .price(price)
                .seatGrade(seatGrade)
                .status(SeatStatus.ASSIGNED)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
