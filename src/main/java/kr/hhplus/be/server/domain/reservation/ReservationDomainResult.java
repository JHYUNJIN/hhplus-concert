package kr.hhplus.be.server.domain.reservation;

import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.seat.Seat;

public record ReservationDomainResult(
        Seat seat,
        Reservation reservation,
        Payment payment,
        ConcertDate concertDate
) {
}
