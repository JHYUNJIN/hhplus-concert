package kr.hhplus.be.server.usecase.reservation.output;

import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.seat.Seat;

import java.util.UUID;

public record CreateReservationResult(
        Reservation reservation,
        Payment payment,
        Seat seat,
        ConcertDate concertDate,
        UUID userId
) {
}