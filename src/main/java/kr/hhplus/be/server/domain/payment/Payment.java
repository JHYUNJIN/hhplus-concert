package kr.hhplus.be.server.domain.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record Payment(
        UUID id,
        UUID userId,
        UUID reservationId,
        BigDecimal amount,
        PaymentStatus status,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static Payment of(UUID userId, UUID reservationId, BigDecimal amount) {
        return Payment.builder()
                .userId(userId)
                .reservationId(reservationId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();
    }

    public Payment success() {
        return Payment.builder()
                .id(id)
                .userId(userId)
                .reservationId(reservationId)
                .amount(amount)
                .status(PaymentStatus.SUCCESS)
                .build();
    }

    public boolean isPaid() {
        return status.equals(PaymentStatus.SUCCESS);
    }

    public boolean checkAmount() {
        return amount().compareTo(BigDecimal.ZERO) > 0;
    }
}
