package kr.hhplus.be.server.payment.port.in.dto;

import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.user.domain.User;
import lombok.Builder;

@Builder
public record PaymentResult(
        Payment payment,
        Seat seat,
        Reservation reservation,
        User user
) {
    public static PaymentResult from(PaymentTransactionResult paymentTransactionResult) {
        return PaymentResult.builder()
                .payment(paymentTransactionResult.payment())
                .seat(paymentTransactionResult.seat())
                .reservation(paymentTransactionResult.reservation())
                .user(paymentTransactionResult.user())
                .build();
    }
}
