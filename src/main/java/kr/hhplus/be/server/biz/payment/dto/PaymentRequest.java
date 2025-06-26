package kr.hhplus.be.server.biz.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {
    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;

    @NotBlank(message = "예약 ID는 필수입니다.")
    private String reservationId;

    @NotNull(message = "결제 금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "결제 금액은 0보다 커야 합니다.")
    private BigDecimal amount;
}