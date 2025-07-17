package kr.hhplus.be.server.domain.reservation;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.seat.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationDomainService {

    // 좌석 예약 처리
    public ReservationDomainResult processReservation(Concert concert, ConcertDate concertDate, Seat seat, UUID userId) throws CustomException {
        validateConcertOpenTimeAndDeadline(concert);
        validateConcertDateDeadline(concertDate); // 콘서트 날짜 마감일 검증
        validateSeatAvailable(seat);

        Seat 		reservedSeat 	= 	seat.reserve();
        Reservation reservation 	= 	Reservation.of(userId, seat.id());
        ConcertDate updatedConcertDate = concertDate.decreaseAvailableSeatCount();

        return new ReservationDomainResult(reservedSeat, reservation, null, concertDate);
    }

    private void validateSeatAvailable(Seat seat) throws CustomException {
        if (!seat.isAvailable())
            throw new CustomException(ErrorCode.ALREADY_RESERVED_SEAT);
    }

    private void validateConcertDateDeadline(ConcertDate concertDate) throws CustomException {
        if (!concertDate.checkDeadline())
            throw new CustomException(ErrorCode.OVER_DEADLINE);
    }

    private void validateConcertOpenTimeAndDeadline(Concert concert) throws CustomException {
        if (!concert.isOpen())
            throw new CustomException(ErrorCode.CONCERT_NOT_OPEN);
    }
}
