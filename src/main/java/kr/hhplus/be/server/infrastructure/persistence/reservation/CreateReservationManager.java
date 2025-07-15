package kr.hhplus.be.server.infrastructure.persistence.reservation;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concert.Concert;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.concertDate.ConcertDateRepository;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.queue.QueueToken;
import kr.hhplus.be.server.domain.queue.QueueTokenRepository;
import kr.hhplus.be.server.domain.queue.QueueTokenUtil;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationDomainResult;
import kr.hhplus.be.server.domain.reservation.ReservationDomainService;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatHoldRepository;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.usecase.reservation.input.ReserveSeatCommand;
import kr.hhplus.be.server.usecase.reservation.output.CreateReservationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateReservationManager {

    private final ReservationRepository reservationRepository;
    private final QueueTokenRepository queueTokenRepository;
    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationDomainService reservationDomainService;

    @Transactional
    public CreateReservationResult processCreateReservation(ReserveSeatCommand command, QueueToken queueToken) throws CustomException {

        Concert concert = getConcert(command.concertId());
        ConcertDate concertDate = getConcertDate(command.concertDateId());
        Seat seat = getSeat(command.seatId(), command.concertDateId());

        ReservationDomainResult result = reservationDomainService.processReservation(concert, concertDate, seat, queueToken.userId());
//        seatHoldRepository.hold(result.seat().id(), queueToken.userId()); 예약생성 성공 이벤트에서 처리
        return processReservation(result, queueToken.userId());
    }

    private CreateReservationResult processReservation(ReservationDomainResult result, UUID userId) {
        Seat savedSeat = seatRepository.save(result.seat());
        Reservation savedReservation = reservationRepository.save(result.reservation());
        Payment savedPayment = paymentRepository.save(Payment.of(userId, savedReservation.id(), savedSeat.price()));

        return new CreateReservationResult(savedReservation, savedPayment, savedSeat, result.concertDate(), userId);
    }

    private Seat getSeat(UUID seatId, UUID concertDateId) throws CustomException {
        return seatRepository.findBySeatIdAndConcertDateId(seatId, concertDateId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));
    }

    private ConcertDate getConcertDate(UUID concertDateId) throws CustomException {
        return concertDateRepository.findById(concertDateId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND));
    }

    private Concert getConcert(UUID concertId) throws CustomException {
        return concertRepository.findById(concertId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_NOT_FOUND));
    }

    private QueueToken getQueueTokenAndValid(ReserveSeatCommand command) throws CustomException {
        QueueToken queueToken = queueTokenRepository.findQueueTokenByTokenId(command.queueTokenId());
        QueueTokenUtil.validateActiveQueueToken(queueToken);
        return queueToken;
    }
}
