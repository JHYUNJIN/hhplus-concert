package kr.hhplus.be.server.reservation.port.in;

import kr.hhplus.be.server.reservation.domain.enums.ReservationStatus;
import kr.hhplus.be.server.reservation.port.in.dto.CreateReservationResult;
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
