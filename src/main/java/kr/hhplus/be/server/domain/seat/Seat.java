package kr.hhplus.be.server.domain.seat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
        return status.equals(kr.hhplus.be.server.domain.seat.SeatStatus.AVAILABLE);
    }

    public Seat reserve() {
        return Seat.builder()
                .id(id)
                .concertDateId(concertDateId)
                .seatNo(seatNo)
                .price(price)
                .seatGrade(seatGrade)
                .status(SeatStatus.RESERVED)
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
