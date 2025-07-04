package kr.hhplus.be.server.domain.event.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.event.Event;
import kr.hhplus.be.server.domain.event.EventTopic;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.user.User;
import lombok.Builder;

@Builder
public record PaymentSuccessEvent (
        UUID paymentId,
        UUID reservationId,
        UUID seatId,
        UUID userId,
        BigDecimal amount,
        LocalDateTime occurredAt
) implements Event {

    public static PaymentSuccessEvent of(Payment savedPayment, Reservation savedReservation, Seat savedSeat, User savedUser) {
        return PaymentSuccessEvent.builder()
                .paymentId(savedPayment.id())
                .reservationId(savedReservation.id())
                .seatId(savedSeat.id())
                .userId(savedUser.id())
                .amount(savedPayment.amount())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    public EventTopic getTopic() {
        return EventTopic.PAYMENT_SUCCESS;
    }

    @Override
    public String getKey() {
        return paymentId.toString();
    }
}
