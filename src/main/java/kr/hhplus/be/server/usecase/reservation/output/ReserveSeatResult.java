package kr.hhplus.be.server.usecase.reservation.output;

import kr.hhplus.be.server.domain.reservation.ReservationStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReserveSeatResult(
        UUID reservationId,
        UUID seatId,
        Integer seatNo,
        BigDecimal price,
        ReservationStatus status,
        LocalDateTime createdAt
) {
    public static ReserveSeatResult from(CreateReservationResult result) {
        return ReserveSeatResult.builder()
                .reservationId(result.reservation().id())
                .seatId(result.seat().id())
                .seatNo(result.seat().seatNo())
                .price(result.seat().price())
                .status(result.reservation().status())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
