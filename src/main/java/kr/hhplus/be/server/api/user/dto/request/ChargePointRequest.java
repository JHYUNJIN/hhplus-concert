package kr.hhplus.be.server.api.user.dto.request;

import java.math.BigDecimal;

public record ChargePointRequest(
        BigDecimal point
) {
}
