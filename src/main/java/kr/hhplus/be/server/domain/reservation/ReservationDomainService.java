package kr.hhplus.be.server.domain.reservation;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.seat.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservationDomainService {

    // 좌석 예약 처리
    public ReservationDomainResult processReservation(ConcertDate concertDate, Seat seat, UUID userId) throws CustomException {
//        validateSeatAvailable(seat); // reserve() 메소드 내부로 이동, 고튜터님 피드백 적용
        validateConcertDateDeadline(concertDate);

        Seat 		reservedSeat 	= 	seat.reserve();
        Reservation reservation 	= 	Reservation.of(userId, seat.id());

        return new ReservationDomainResult(reservedSeat, reservation);
    }

//    private void validateSeatAvailable(Seat seat) throws CustomException {
//        if (!seat.isAvailable())
//            throw new CustomException(ErrorCode.ALREADY_RESERVED_SEAT);
//    }

    private void validateConcertDateDeadline(ConcertDate concertDate) throws CustomException {
        if (!concertDate.checkDeadline())
            throw new CustomException(ErrorCode.OVER_DEADLINE);
    }
}
