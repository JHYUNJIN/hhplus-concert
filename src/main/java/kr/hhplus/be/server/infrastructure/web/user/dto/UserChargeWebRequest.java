package kr.hhplus.be.server.infrastructure.web.user.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UserChargeWebRequest {
    @NotBlank(message = "사용자 ID는 필수입니다.")
    private String userId;

    @NotNull(message = "충전 금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "충전 금액은 0보다 커야 합니다.")
    private BigDecimal amount;
}