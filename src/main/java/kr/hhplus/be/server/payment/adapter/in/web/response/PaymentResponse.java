package kr.hhplus.be.server.payment.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.hhplus.be.server.payment.domain.enums.PaymentStatus;
import kr.hhplus.be.server.payment.port.in.dto.PaymentResult;
import lombok.Builder;

@Builder
public record PaymentResponse(
        @Schema(description = "결제 ID")
        UUID paymentId,
        @Schema(description = "예약 ID")
        UUID reservationId,
        @Schema(description = "결제 금액")
        BigDecimal amount,
        @Schema(description = "결제 상태")
        PaymentStatus status,
        @Schema(description = "결제 시간")
        LocalDateTime createdAt
) {
    public static PaymentResponse from(PaymentResult paymentResult) {
        return PaymentResponse.builder()
                .paymentId(paymentResult.payment().id())
                .reservationId(paymentResult.reservation().id())
                .amount(paymentResult.payment().amount())
                .status(paymentResult.payment().status())
                .createdAt(paymentResult.payment().updatedAt())
                .build();
    }
}
