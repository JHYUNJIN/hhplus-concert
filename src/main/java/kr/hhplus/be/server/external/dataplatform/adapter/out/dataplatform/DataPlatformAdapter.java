package kr.hhplus.be.server.external.dataplatform.adapter.out.dataplatform;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.external.dataplatform.adapter.out.dataplatform.request.ReservationDataRequest;
import kr.hhplus.be.server.external.dataplatform.port.out.DataPlatformOutPort;
import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataPlatformAdapter implements DataPlatformOutPort {

	private final DataPlatformClient dataPlatformClient;

	@Override
	public void send(PaymentSuccessEvent event, Concert concert) {
		dataPlatformClient.sendReservationData(ReservationDataRequest.of(event, concert));
	}
}
