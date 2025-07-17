package kr.hhplus.be.server.payment.usecase;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.payment.port.in.dto.PaymentDomainResult;
import kr.hhplus.be.server.payment.port.in.dto.PaymentTransactionResult;
import kr.hhplus.be.server.payment.domain.PaymentFailedEvent;
import kr.hhplus.be.server.payment.domain.Payment;
import kr.hhplus.be.server.payment.domain.enums.PaymentStatus;
import kr.hhplus.be.server.payment.port.out.PaymentRepository;
import kr.hhplus.be.server.queue.domain.QueueToken;
import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
import kr.hhplus.be.server.reservation.domain.Reservation;
import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
import kr.hhplus.be.server.concert.domain.Seat;
import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
import kr.hhplus.be.server.concert.port.out.SeatRepository;
import kr.hhplus.be.server.user.domain.User;
import kr.hhplus.be.server.user.port.out.UserRepository;
import kr.hhplus.be.server.payment.port.out.EventPublisher;
import kr.hhplus.be.server.payment.port.in.dto.PaymentCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentManager {

    private final QueueTokenRepository queueTokenRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final PaymentDomainService paymentDomainService;
    private final EventPublisher eventPublisher;

    /* 결제 상태 흐름
    [PENDING]
   │
   ├── (updateStatusIfExpected → PROCESSING 성공)
   │       │
   │       ├── [processPayment 성공] → update → SUCCESS
   │       │
   │       └── [예외 발생] → rollback → FAILED
   │
   └── (이미 처리됨) → ALREADY_PROCESSED 예외
     */
    @Transactional
    public PaymentTransactionResult processPayment(PaymentCommand command, QueueToken queueToken) throws CustomException {
        // 객체 조회 및 유효성 검사
        User user = getUser(queueToken.userId());
        Reservation reservation = getReservation(command.reservationId());
        Seat seat = getSeat(reservation.seatId());
        Payment payment = getPayment(reservation.id());
        validateSeatHold(seat.id(), user.id());
        try{
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
            PaymentDomainResult result = paymentDomainService.processPayment(reservation, payment, seat, user);
            PaymentTransactionResult paymentTransactionResult = processPayment(result);

            // 결제 상태 SUCCESS 업데이트
            paymentRepository.updateStatusIfExpected(
                    payment.id(),
                    PaymentStatus.SUCCESS,
                    PaymentStatus.PROCESSING
            );

            // 좌석해제 및 토큰 만료 처리 -> 이벤트 발행으로 대체
//            seatHoldRepository.deleteHold(paymentTransactionResult.seat().id(), paymentTransactionResult.user().id());
//            queueTokenRepository.expiresQueueToken(queueToken.tokenId().toString());

            return paymentTransactionResult;
        } catch (CustomException e) {
            eventPublisher.publish(PaymentFailedEvent.of(queueToken, payment, reservation, seat, user, e.getErrorCode()));
            throw e;
        }
    }

    private PaymentTransactionResult processPayment(PaymentDomainResult result) {
        Payment savedPayment	 = paymentRepository.save(result.payment());
        User savedUser           = userRepository.save(result.user());
        Reservation savedReservation = reservationRepository.save(result.reservation());
        Seat savedSeat        = seatRepository.save(result.seat());

        return new PaymentTransactionResult(savedPayment, savedReservation, savedSeat, savedUser);
    }

    private User getUser(UUID userId) throws CustomException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
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
