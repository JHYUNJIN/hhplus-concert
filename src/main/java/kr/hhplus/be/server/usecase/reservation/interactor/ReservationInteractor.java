package kr.hhplus.be.server.usecase.reservation.interactor;

import kr.hhplus.be.server.usecase.event.EventPublisher;
import kr.hhplus.be.server.usecase.reservation.input.ReservationInput;
import kr.hhplus.be.server.usecase.reservation.input.ReserveSeatCommand;
import kr.hhplus.be.server.usecase.reservation.output.ReservationOutput;
import kr.hhplus.be.server.usecase.reservation.output.ReserveSeatResult;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.concert.ConcertRepository;
import kr.hhplus.be.server.domain.concertDate.ConcertDate;
import kr.hhplus.be.server.domain.concertDate.ConcertDateRepository;
import kr.hhplus.be.server.domain.event.reservation.ReservationCreatedEvent;
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
import kr.hhplus.be.server.domain.seat.SeatLockRepository;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReservationInteractor implements ReservationInput {

    private final QueueTokenRepository queueTokenRepository;
    private final SeatLockRepository seatLockRepository;
    private final ConcertRepository concertRepository;
    private final ConcertDateRepository concertDateRepository;
    private final SeatRepository seatRepository;
    private final ReservationDomainService reservationDomainService;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final EventPublisher eventPublisher;
    private final ReservationOutput reservationOutput;


    @Override
    @Transactional
    public void reserveSeat(ReserveSeatCommand command) throws CustomException {
        System.out.println("🚀[로그:정현진] reserveSeat들어옴");
        System.out.println("🚀[로그:정현진] command : " + command);

        boolean lockAcquired = false;

        try{
            // 토큰 조회
            QueueToken queueToken = getQueueTokenAndValid(command);
            System.out.println("🚀[로그:정현진] queueToken : " + queueToken);
            // 콘서트 존재 여부 확인
            checkExistsConcert(command.concertId());
            // 콘스터 날짜 조회
            ConcertDate concertDate = getConcertDate(command.concertDateId());
            // 좌석 조회
            Seat seat = getSeat(command.seatId(), command.concertDateId());
            System.out.println("🚀[로그:정현진] seat : " + seat);
            // 예약 가능한 좌석인지 확인
            lockAcquired = acquisitionSeatLock(command.seatId());
            System.out.println("🚀[로그:정현진] lockAcquired : " + lockAcquired);
            // 좌석 예약 처리
            ReservationDomainResult result = reservationDomainService.processReservation(concertDate, seat, queueToken.userId());
            System.out.println("🚀[로그:정현진] result : " + result);
            System.out.println("🚀[로그:정현진] result.seat() : " + result.seat());

            // DB 저장 (DB 트랜잭션 범위)
            Seat savedSeat = seatRepository.save(result.seat());
            System.out.println("🚀[로그:정현진] savedSeat : " + savedSeat);
            Reservation savedReservation = reservationRepository.save(result.reservation());
            System.out.println("🚀[로그:정현진] savedReservation : " + savedReservation);
            Payment savedPayment = paymentRepository.save(Payment.of(savedSeat.id(), savedReservation.id(), savedSeat.price()));
            System.out.println("🚀[로그:정현진] savedPayment : " + savedPayment);

            // Redis 좌석 홀드 (DB 트랜잭션과 무관하게 즉시 실행될 수 있음. 이 또한 이벤트로 옮기는 것을 고려)
            seatHoldRepository.hold(seat.id(), queueToken.userId());
            // 예약 생성 이벤트 발행
            eventPublisher.publish(
                    ReservationCreatedEvent.of(savedPayment, savedReservation, savedSeat, queueToken.userId()));
            // 예약 결과 객체 생성 및 반환
            reservationOutput.ok(ReserveSeatResult.of(savedReservation, savedSeat));

        }catch (CustomException e) {
            ErrorCode errorCode = e.getErrorCode();
            log.warn("좌석 예약중 비즈니스 예외 발생 - {}, {}", errorCode.getCode(), errorCode.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("좌석 예약중 예외 발생 - {}", ErrorCode.INTERNAL_SERVER_ERROR, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (lockAcquired) seatLockRepository.releaseLock(command.seatId());
        }

    }
        
    private QueueToken getQueueTokenAndValid(ReserveSeatCommand command) throws CustomException {
        QueueToken queueToken = queueTokenRepository.findQueueTokenByTokenId(command.queueTokenId());
        QueueTokenUtil.validateActiveQueueToken(queueToken);
        return queueToken;
    }

    private void checkExistsConcert(UUID concertId) throws CustomException {
        if (!concertRepository.existsById(concertId))
            throw new CustomException(ErrorCode.CONCERT_NOT_FOUND);
    }

    private ConcertDate getConcertDate(UUID concertDateId) throws CustomException {
        return concertDateRepository.findById(concertDateId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND));
    }

    private Seat getSeat(UUID seatId, UUID concertDateId) throws CustomException {
        return seatRepository.findBySeatIdAndConcertDateId(seatId, concertDateId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));
    }

    private boolean acquisitionSeatLock(UUID seatId) throws CustomException {
        if (!seatLockRepository.acquisitionLock(seatId))
            throw new CustomException(ErrorCode.SEAT_LOCK_CONFLICT);

        return true;
    }

}

