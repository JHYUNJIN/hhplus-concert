package kr.hhplus.be.server.external.dataplatform.adapter.out.dataplatform.request;

import kr.hhplus.be.server.concert.domain.Concert;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ReservationDataRequest(
		UUID reservationId,
		UUID paymentId,
		UUID concertId,
		UUID seatId
) {
	public static ReservationDataRequest of(UUID reservationId, UUID paymentId, UUID seatId, Concert concert) {
		return ReservationDataRequest.builder()
			.reservationId(reservationId)
			.paymentId(paymentId)
			.concertId(concert.id())
			.seatId(seatId)
			.build();
	}
}