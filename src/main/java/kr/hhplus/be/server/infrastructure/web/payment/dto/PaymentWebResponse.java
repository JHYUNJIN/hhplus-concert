package kr.hhplus.be.server.infrastructure.web.payment.dto;

import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebResponse {
    private String paymentId;
    private String userId;
    private String reservationId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PaymentWebResponse from(Payment payment) {
        return new PaymentWebResponse(
                payment.getId(),
                payment.getUser().getId(),
                payment.getReservation().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getFailureReason(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}