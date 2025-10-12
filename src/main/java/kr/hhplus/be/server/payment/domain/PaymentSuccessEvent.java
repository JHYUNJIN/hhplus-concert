package kr.hhplus.be.server.payment.domain;

import java.time.LocalDateTime;

import kr.hhplus.be.server.common.event.Event;
import kr.hhplus.be.server.common.event.EventTopic;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.concert.domain.Seat;
// import kr.hhplus.be.server.user.domain.User; // 삭제
import kr.hhplus.be.server.payment.port.in.dto.PaymentTransactionResult;
import lombok.Builder;

import java.util.UUID; // UUID 임포트 추가

@Builder
public record PaymentSuccessEvent (
        Payment payment,
        Reservation reservation,
        Seat seat,
        UUID userId, // User 객체 대신 userId 사용
        QueueToken queueToken,
        LocalDateTime occurredAt
) implements Event {

    public static PaymentSuccessEvent from(PaymentTransactionResult paymentTransactionResult) {
        return PaymentSuccessEvent.builder()
                .payment(paymentTransactionResult.payment())
                .reservation(paymentTransactionResult.reservation())
                .seat(paymentTransactionResult.seat())
                .userId(paymentTransactionResult.userId()) // user 대신 userId 사용
                .queueToken(paymentTransactionResult.queueToken())
                .occurredAt(java.time.LocalDateTime.now())
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
