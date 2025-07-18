package kr.hhplus.be.server.external.dataplatform.adapter.out.dataplatform.request;

import java.util.UUID;

import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;
import lombok.Builder;

@Builder
public record ReservationDataRequest (
	UUID reservationId,
	UUID paymentId,
	UUID concertId,
	UUID seatId
) {
	public static ReservationDataRequest of(PaymentSuccessEvent event, Concert concert) {
		return ReservationDataRequest.builder()
			.reservationId(event.reservation().id())
			.paymentId(event.payment().id())
			.concertId(concert.id())
			.seatId(event.seat().id())
			.build();
	}
}
