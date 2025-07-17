package kr.hhplus.be.server.payment.port.in.dto;

import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.payment.domain.Payment;

public record PaymentDomainResult(
        User user,
        Reservation reservation,
        Payment payment,
        Seat seat
) {
}
