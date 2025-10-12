package kr.hhplus.be.server.payment.usecase;

import kr.hhplus.be.server.common.event.EventPublisher;
import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.concert.domain.ConcertDate;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.concert.port.out.ConcertDateRepository;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.external.UserApiClient;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.domain.PaymentFailedEvent;
import kr.hhplus.be.server.payment.domain.enums.PaymentStatus;
import kr.hhplus.be.server.payment.port.in.dto.PaymentCommand;
import kr.hhplus.be.server.payment.port.in.dto.PaymentDomainResult;
import kr.hhplus.be.server.payment.port.in.dto.PaymentTransactionResult;
import kr.hhplus.be.server.payment.port.out.PaymentRepository;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentManager {

    private final QueueTokenRepository queueTokenRepository;
    private final ReservationRepository reservationRepository;
    private final UserApiClient userApiClient;
    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final ConcertDateRepository concertDateRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final PaymentDomainService paymentDomainService;
    private final EventPublisher eventPublisher;

    @Transactional
    public PaymentTransactionResult processPayment(PaymentCommand command, QueueToken queueToken) throws CustomException {
        // 객체 조회 및 유효성 검사
        BigDecimal userBalance = userApiClient.getUserBalance(queueToken.userId()).block();

        Reservation reservation = getReservation(command.reservationId());
        Seat seat = getSeat(reservation.seatId());
        Payment payment = getPayment(reservation.id());
        ConcertDate concertDate = getConcertDate(seat.concertDateId());
        validateSeatHold(seat.id(), queueToken.userId());

        try {
            // 🔐 낙관적 락: 상태 선점 (PENDING → PROCESSING)
            int updated = paymentRepository.updateStatusIfExpected(
                    payment.id(),
                    PaymentStatus.PROCESSING,
                    PaymentStatus.PENDING
            );
            if (updated != 1) {
                throw new CustomException(ErrorCode.ALREADY_PROCESSED, "결제가 이미 처리되었습니다.");
            }

            // 결제 객체 상태 변경 후 결제 진행
            payment = payment.toProcessing();
            PaymentDomainResult result = paymentDomainService.processPayment(reservation, payment, seat, queueToken.userId(), userBalance, queueToken);
            PaymentTransactionResult paymentTransactionResult = processPayment(result);

            // 결제 상태 SUCCESS 업데이트
            paymentRepository.updateStatusIfExpected(
                    payment.id(),
                    PaymentStatus.SUCCESS,
                    PaymentStatus.PROCESSING
            );

            return paymentTransactionResult;
        } catch (CustomException e) {
            eventPublisher.publish(PaymentFailedEvent.of(queueToken, payment, reservation, seat, concertDate, queueToken.userId(), e.getErrorCode()));
            throw e;
        }
    }

    private PaymentTransactionResult processPayment(PaymentDomainResult result) {
        Payment savedPayment = paymentRepository.save(result.payment());
        userApiClient.useUserBalance(result.userId(), result.payment().amount()).block();

        Reservation savedReservation = reservationRepository.save(result.reservation());
        Seat savedSeat = seatRepository.save(result.seat());

        return new PaymentTransactionResult(savedPayment, savedReservation, savedSeat, result.userId(), result.queueToken());
    }

    private ConcertDate getConcertDate(UUID concertDateId) throws CustomException {
        return concertDateRepository.findById(concertDateId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONCERT_DATE_NOT_FOUND));
    }

    private Seat getSeat(UUID seatId) throws CustomException {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));
    }

    private Payment getPayment(UUID reservationId) throws CustomException {
        return paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private Reservation getReservation(UUID reservationId) throws CustomException {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateSeatHold(UUID seatId, UUID userId) throws CustomException {
        if (!seatHoldRepository.isHoldSeat(seatId, userId))
            throw new CustomException(ErrorCode.SEAT_NOT_HOLD);
    }
}
