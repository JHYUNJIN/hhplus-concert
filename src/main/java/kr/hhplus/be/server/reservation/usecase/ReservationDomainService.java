package kr.hhplus.be.server.reservation.usecase;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.port.in.dto.ReservationDomainResult;
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

        return new ReservationDomainResult(reservedSeat, reservation, null, updatedConcertDate);
    }

//    public ReservationDomainResult processReservationExpired(Reservation reservation, Payment payment, Seat seat)
//            throws CustomException {
//        validateExpiredStatus(reservation, payment, seat);
//
//        Seat expiredSeat = seat.expire();
//        Reservation expiredReservation = reservation.expire();
//        Payment expiredPayment = payment.expire();
//
//        return new ReservationDomainResult(expiredSeat, expiredReservation, expiredPayment, null);
//    }

//    private void validateExpiredStatus(Reservation reservation, Payment payment, Seat seat) throws CustomException {
//        if (!reservation.isPending())
//            throw new CustomException(ErrorCode.RESERVATION_STATUS_NOT_PENDING);
//        if (!payment.isPending())
//            throw new CustomException(ErrorCode.PAYMENT_STATUS_NOT_PENDING);
//        if (!seat.isReserved())
//            throw new CustomException(ErrorCode.SEAT_STATUS_NOT_RESERVED);
//    }

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
