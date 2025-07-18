package kr.hhplus.be.server.reservation.usecase;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.concert.domain.Concert;
import kr.hhplus.be.server.concert.port.out.ConcertRepository;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.port.out.PaymentRepository;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import kr.hhplus.be.server.queue.domain.QueueTokenUtil;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.port.in.dto.ReservationDomainResult;
import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.reservation.port.in.dto.ReserveSeatCommand;
import kr.hhplus.be.server.reservation.port.in.dto.CreateReservationResult;
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
        // 예약 생성 성공 시, 좌석 보유 상태를 업데이트
//        seatHoldRepository.hold(result.seat().id(), queueToken.userId()); 예약생성 성공 이벤트에서 처리
        return processReservation(result, queueToken.userId());
    }

    private CreateReservationResult processReservation(ReservationDomainResult result, UUID userId) {
        Seat savedSeat = seatRepository.save(result.seat());
        Reservation savedReservation = reservationRepository.save(result.reservation());
        Payment savedPayment = paymentRepository.save(Payment.of(userId, savedReservation.id(), savedSeat.price()));
        ConcertDate savedConcertDate = concertDateRepository.save(result.concertDate());

        return new CreateReservationResult(savedReservation, savedPayment, savedSeat, savedConcertDate, userId);
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
