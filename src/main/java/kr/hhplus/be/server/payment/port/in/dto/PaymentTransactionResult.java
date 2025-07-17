package kr.hhplus.be.server.payment.port.in.dto;

import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.user.domain.User;

public record PaymentTransactionResult(
        Payment payment,
        Reservation reservation,
        Seat seat,
        User user
) {
}
