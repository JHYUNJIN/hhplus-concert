package kr.hhplus.be.server.domain.event.reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.event.Event;
import kr.hhplus.be.server.domain.event.EventTopic;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.reservation.Reservation;
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

    public static ReservationCreatedEvent of(UUID reservationId, UUID userId, UUID paymentId, UUID seatId, BigDecimal amount, LocalDateTime now) {
        return ReservationCreatedEvent.builder()
                .reservationId(reservationId)
                .userId(userId)
                .paymentId(paymentId)
                .seatId(seatId)
                .amount(amount)
                .expiresAt(now.plusMinutes(5))
                .occurredAt(now)
                .build();
    }

    public static ReservationCreatedEvent of(Payment savedPayment, Reservation savedReservation, Seat savedSeat, UUID userId) {
        LocalDateTime now = LocalDateTime.now();

        return ReservationCreatedEvent.builder()
                .reservationId(savedReservation.id())
                .userId(userId)
                .paymentId(savedPayment.id())
                .seatId(savedSeat.id())
                .amount(savedPayment.amount())
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
