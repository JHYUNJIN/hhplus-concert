package kr.hhplus.be.server.payment.port.in.dto;

import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.payment.domain.Payment;

public record PaymentDomainResult(
        QueueToken queueToken,
        User user,
        Reservation reservation,
        Payment payment,
        Seat seat
) {
}
