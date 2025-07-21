package kr.hhplus.be.server.external.dataplatform.port.in;

import java.util.UUID;

public interface PaymentSuccessUseCase {
	void sendDataPlatform(UUID reservationId, UUID paymentId, UUID seatId, UUID concertDateId);
}