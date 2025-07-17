package kr.hhplus.be.server.usecase.payment.input;

public interface PaymentInput {
    void payment(PaymentCommand commend) throws Exception;
}
