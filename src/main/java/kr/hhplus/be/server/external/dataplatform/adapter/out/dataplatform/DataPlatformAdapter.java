package kr.hhplus.be.server.external.dataplatform.adapter.out.dataplatform;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.external.dataplatform.adapter.out.dataplatform.request.ReservationDataRequest;
import kr.hhplus.be.server.external.dataplatform.port.out.DataPlatformOutPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataPlatformAdapter implements DataPlatformOutPort {

	private final DataPlatformClient dataPlatformClient;

	@Override
	public void send(UUID reservationId, UUID paymentId, UUID seatId, Concert concert) {
		dataPlatformClient.sendReservationData(
				ReservationDataRequest.of(reservationId, paymentId, seatId, concert)
		);
	}
}