package kr.hhplus.be.server.infrastructure.api.user.dto.request;

import java.math.BigDecimal;

public record ChargePointRequest(
        BigDecimal point
) {
}
