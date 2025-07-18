package kr.hhplus.be.server.payment.port.in;

import kr.hhplus.be.server.payment.port.in.dto.PaymentCommand;

public interface PaymentInput {
    void payment(PaymentCommand commend) throws Exception;
}
