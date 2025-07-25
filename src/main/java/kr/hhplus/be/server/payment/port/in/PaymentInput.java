package kr.hhplus.be.server.payment.port.in;

import kr.hhplus.be.server.payment.port.in.dto.PaymentCommand;
import kr.hhplus.be.server.payment.port.in.dto.PaymentResult;

public interface PaymentInput {
    PaymentResult payment(PaymentCommand commend) throws Exception;
}
