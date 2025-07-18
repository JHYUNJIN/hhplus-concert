package kr.hhplus.be.server.payment.domain;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.event.Event;
import kr.hhplus.be.server.common.event.EventTopic;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.user.domain.User;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PaymentFailedEvent(
        UUID tokenId,
        UUID paymentId,
        UUID reservationId,
        UUID userId,
        UUID seatId,
        UUID concertDateId,
        BigDecimal amount,
        ErrorCode errorCode,
        LocalDateTime occurredAt
) implements Event {

    public static PaymentFailedEvent of(QueueToken queueToken, Payment payment, Reservation reservation, Seat seat, ConcertDate concertDate, User user, ErrorCode errorCode) {
        return PaymentFailedEvent.builder()
                .tokenId(queueToken.tokenId())
                .paymentId(payment.id())
                .reservationId(reservation.id())
                .seatId(seat.id())
                .concertDateId(concertDate.id())
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
