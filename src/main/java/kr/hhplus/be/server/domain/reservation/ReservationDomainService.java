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
        validateConcertDateDeadline(concertDate); // 콘서트 날짜 마감일 검증

        Seat 		reservedSeat 	= 	seat.reserve();
        Reservation reservation 	= 	Reservation.of(userId, seat.id());
        ConcertDate updatedConcertDate = concertDate.decreaseAvailableSeatCount();
        System.out.println("🚀[로그:정현진] 업데이트 availableSeatCount : " + updatedConcertDate.availableSeatCount());

        return new ReservationDomainResult(reservedSeat, reservation, updatedConcertDate);
    }

    private void validateConcertDateDeadline(ConcertDate concertDate) throws CustomException {
        if (!concertDate.checkDeadline())
            throw new CustomException(ErrorCode.OVER_DEADLINE);
    }
}
