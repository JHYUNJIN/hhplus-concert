package kr.hhplus.be.server.domain.event.payment;

import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.event.Event;
import kr.hhplus.be.server.domain.event.EventTopic;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.user.User;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentFailedEvent(
        UUID paymentId,
        UUID reservationId,
        UUID userId,
        UUID seatId,
        BigDecimal amount,
        ErrorCode errorCode,
        LocalDateTime occurredAt
) implements Event {

    public static PaymentFailedEvent of(Payment payment, Reservation reservation, Seat seat, User user, ErrorCode errorCode) {
        return PaymentFailedEvent.builder()
                .paymentId(payment.id())
                .reservationId(reservation.id())
                .seatId(seat.id())
                .userId(user.id())
                .amount(payment.amount())
                .errorCode(errorCode)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    public String getKey() {
        return reservationId.toString();
    }

    @Override
    public EventTopic getTopic() {
        return EventTopic.PAYMENT_FAILED;
    }
}
