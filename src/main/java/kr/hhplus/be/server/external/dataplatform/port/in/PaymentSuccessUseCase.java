package kr.hhplus.be.server.external.dataplatform.port.in;

import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;

public interface PaymentSuccessUseCase {
	void sendDataPlatform(PaymentSuccessEvent event);
}
