package kr.hhplus.be.server.usecase.payment.output;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.infrastructure.persistence.payment.PaymentTransactionResult;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.user.User;
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
