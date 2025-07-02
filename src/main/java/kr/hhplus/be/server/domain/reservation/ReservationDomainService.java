package kr.hhplus.be.server.domain.reservation;

import java.util.UUID;

import org.springframework.stereotype.Service;

import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationDomainService {

    public ReservationDomainResult processReservation(ConcertDate concertDate, Seat seat, UUID userId) throws CustomException {
        validateSeatAvailable(seat);
        validateConcertDateDeadline(concertDate);

        Seat 		reservedSeat 	= 	seat.reserve();
        Reservation reservation 	= 	Reservation.of(userId, seat.id());

        return new ReservationDomainResult(reservedSeat, reservation);
    }

    private void validateSeatAvailable(Seat seat) throws CustomException {
        if (!seat.isAvailable())
            throw new CustomException(ErrorCode.ALREADY_RESERVED_SEAT);
    }

    private void validateConcertDateDeadline(ConcertDate concertDate) throws CustomException {
        if (!concertDate.checkDeadline())
            throw new CustomException(ErrorCode.OVER_DEADLINE);
    }
}
