package kr.hhplus.be.server.domain.event.payment.success.event;

import java.time.LocalDateTime;

import kr.hhplus.be.server.domain.event.Event;
import kr.hhplus.be.server.domain.event.EventTopic;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.infrastructure.persistence.payment.PaymentTransactionResult;
import lombok.Builder;

@Builder
public record PaymentSuccessEvent (
        Payment payment,
        Reservation reservation,
        Seat seat,
        User user,
        LocalDateTime occurredAt
) implements Event {

    public static PaymentSuccessEvent from(PaymentTransactionResult paymentTransactionResult) {
        return PaymentSuccessEvent.builder()
                .payment(paymentTransactionResult.payment())
                .reservation(paymentTransactionResult.reservation())
                .seat(paymentTransactionResult.seat())
                .user(paymentTransactionResult.user())
                .occurredAt(LocalDateTime.now())
                .build();
    }

    @Override
    public EventTopic getTopic() {
        return EventTopic.PAYMENT_SUCCESS;
    }

    @Override
    public String getKey() {
        return payment.id().toString();
    }
}
