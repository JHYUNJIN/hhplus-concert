package kr.hhplus.be.server.payment.port.in.dto;

import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.concert.domain.Seat;
// import kr.hhplus.be.server.user.domain.User; // 삭제
import kr.hhplus.be.server.payment.port.in.dto.PaymentTransactionResult;
import lombok.Builder;

import java.util.UUID; // UUID 임포트 추가

@Builder
public record PaymentResult(
        Payment payment,
        Seat seat,
        Reservation reservation,
        UUID userId // User 객체 대신 userId 사용
) {
    public static PaymentResult from(PaymentTransactionResult paymentTransactionResult) {
        return PaymentResult.builder()
                .payment(paymentTransactionResult.payment())
                .seat(paymentTransactionResult.seat())
                .reservation(paymentTransactionResult.reservation())
                .userId(paymentTransactionResult.userId()) // user 대신 userId 사용
                .build();
    }
}
