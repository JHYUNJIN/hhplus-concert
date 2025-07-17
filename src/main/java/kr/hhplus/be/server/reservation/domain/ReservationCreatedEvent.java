package kr.hhplus.be.server.reservation.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import kr.hhplus.be.server.common.event.Event;
import kr.hhplus.be.server.common.event.EventTopic;
import kr.hhplus.be.server.reservation.port.in.dto.CreateReservationResult;
import lombok.Builder;

@Builder
public record ReservationCreatedEvent(
        UUID reservationId,
        UUID userId,
        UUID paymentId,
        UUID seatId,
        BigDecimal amount,
        LocalDateTime expiresAt,
        LocalDateTime occurredAt
) implements Event {

    public static ReservationCreatedEvent from(CreateReservationResult result) {
        LocalDateTime now = LocalDateTime.now();
        return ReservationCreatedEvent.builder()
                .reservationId(result.reservation().id())
                .userId(result.userId())
                .paymentId(result.payment().id())
                .seatId(result.seat().id())
                .amount(result.payment().amount())
                .expiresAt(now.plusMinutes(5))
                .occurredAt(now)
                .build();
    }

    @Override
    public String getKey() {
        return reservationId.toString();
    }

    @Override
    public EventTopic getTopic() {
        return EventTopic.RESERVATION_CREATED;
    }
}
