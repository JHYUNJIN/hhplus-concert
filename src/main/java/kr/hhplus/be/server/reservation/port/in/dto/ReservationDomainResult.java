package kr.hhplus.be.server.reservation.port.in.dto;

import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.reservation.domain.Reservation;

public record ReservationDomainResult(
        Seat seat,
        Reservation reservation,
        Payment payment,
        ConcertDate concertDate
) {
}
