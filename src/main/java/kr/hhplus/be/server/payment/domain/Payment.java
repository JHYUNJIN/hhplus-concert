package kr.hhplus.be.server.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import kr.hhplus.be.server.payment.domain.enums.PaymentStatus;
import lombok.Builder;

@Builder
public record Payment(
        UUID id,
        UUID userId,
        UUID reservationId,
        BigDecimal amount,
        PaymentStatus status, // 대기, 성공, 실패, 취소
        String failureReason, // 결제 실패 사유
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

    public Payment toProcessing() {
        return Payment.builder()
                .id(id)
                .userId(userId)
                .reservationId(reservationId)
                .amount(amount)
                .status(PaymentStatus.PROCESSING)
                .build();
    }

    public boolean isPaid() {
        return status.equals(PaymentStatus.SUCCESS);
    }

    public boolean checkAmount() {
        return amount().compareTo(BigDecimal.ZERO) > 0;
    }

    public Payment fail() {
        return Payment.builder()
                .id(id)
                .userId(userId)
                .reservationId(reservationId)
                .amount(amount)
                .status(PaymentStatus.FAILED)
                .build();
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public Payment expire() {
        return Payment.builder()
                .id(id)
                .userId(userId)
                .reservationId(reservationId)
                .amount(amount)
                .status(PaymentStatus.FAILED)
                .failureReason("임시 배정이 만료되었습니다.")
                .createdAt(createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
