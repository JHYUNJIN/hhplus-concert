package kr.hhplus.be.server.reservation.port.in.dto;

import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.concert.domain.Seat;

import java.util.UUID;

public record CreateReservationResult(
        Reservation reservation,
        Payment payment,
        Seat seat,
        ConcertDate concertDate,
        UUID userId
) {
}