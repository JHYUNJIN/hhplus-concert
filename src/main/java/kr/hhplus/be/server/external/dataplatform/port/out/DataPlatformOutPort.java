package kr.hhplus.be.server.external.dataplatform.port.out;

import kr.hhplus.be.server.concert.domain.Concert;
import java.util.UUID;

public interface DataPlatformOutPort {
	void send(UUID reservationId, UUID paymentId, UUID seatId, Concert concert);
}