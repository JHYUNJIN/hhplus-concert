package kr.hhplus.be.server.domain.payment;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.user.User;

public record PaymentDomainResult(
        User user,
        Reservation reservation,
        Payment payment,
        Seat seat
) {
}
