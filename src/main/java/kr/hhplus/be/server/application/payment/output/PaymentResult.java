package kr.hhplus.be.server.application.payment.output;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentStatus;
import lombok.Builder;

@Builder
public record PaymentResult(
        UUID paymentId,
        UUID userId,
        UUID reservationId,
        UUID seatId,
        Integer seatNo,
        BigDecimal price,
        PaymentStatus status,
        LocalDateTime createdAt
) {
    public static PaymentResult of(Payment payment, Seat seat, UUID reservationId, UUID userId) {
        return PaymentResult.builder()
                .paymentId(payment.id())
                .userId(userId)
                .reservationId(reservationId)
                .seatId(seat.id())
                .seatNo(seat.seatNo())
                .price(payment.amount())
                .status(payment.status())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
