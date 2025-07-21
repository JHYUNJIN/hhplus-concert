package kr.hhplus.be.server.payment.port.in;

import kr.hhplus.be.server.common.exception.ErrorCode;
import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentFailureCompensationUseCase {
    void compensate(
            UUID paymentId,
            UUID userId,
            UUID reservationId,
            UUID seatId,
            UUID concertDateId,
            String tokenId,
            BigDecimal amount,
            ErrorCode errorCode
    );
}