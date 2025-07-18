package kr.hhplus.be.server.external.dataplatform.port.out;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;

public interface DataPlatformOutPort {
	void send(PaymentSuccessEvent event, Concert concert);
}
