package kr.hhplus.be.server.payment.port.in;

import kr.hhplus.be.server.payment.port.in.dto.PaymentResult;

public interface PaymentOutput {
    void ok(PaymentResult paymentResult);
}
