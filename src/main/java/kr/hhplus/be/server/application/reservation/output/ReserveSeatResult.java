package kr.hhplus.be.server.application.reservation.output;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import lombok.Builder;

@Builder
public record ReserveSeatResult(
        UUID reservationId,
        UUID seatId,
        Integer seatNo,
        BigDecimal price,
        ReservationStatus status,
        LocalDateTime createdAt
) {
    public static ReserveSeatResult of(Reservation reservation, Seat seat) {
        return ReserveSeatResult.builder()
                .reservationId(reservation.id())
                .seatId(seat.id())
                .seatNo(seat.seatNo())
                .price(seat.price())
                .status(reservation.status())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
