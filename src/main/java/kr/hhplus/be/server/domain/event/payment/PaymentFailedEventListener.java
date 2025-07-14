package kr.hhplus.be.server.domain.event.payment;

import kr.hhplus.be.server.common.exception.CustomException;
import kr.hhplus.be.server.common.exception.enums.ErrorCode;
import kr.hhplus.be.server.domain.payment.Payment;
import kr.hhplus.be.server.domain.payment.PaymentRepository;
import kr.hhplus.be.server.domain.reservation.Reservation;
import kr.hhplus.be.server.domain.reservation.ReservationRepository;
import kr.hhplus.be.server.domain.seat.Seat;
import kr.hhplus.be.server.domain.seat.SeatRepository;
import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedEventListener {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final SeatRepository seatRepository;

    // 잔액이 차감되기 전 발생하는 비즈니스 로직 오류 코드 목록
    // 이 목록에 있는 오류로 결제가 실패한 경우, 잔액 복원 로직을 실행하지 않음
    private static final Set<ErrorCode> PRE_CHARGE_ERRORS = Set.of(
            ErrorCode.INSUFFICIENT_BALANCE,
            ErrorCode.INVALID_PAYMENT_AMOUNT,
            ErrorCode.ALREADY_PAID,
            ErrorCode.ALREADY_PROCESSED,
            ErrorCode.RESERVATION_NOT_FOUND,
            ErrorCode.PAYMENT_NOT_FOUND,
            ErrorCode.SEAT_NOT_FOUND,
            ErrorCode.SEAT_NOT_HOLD,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.INVALID_QUEUE_TOKEN
    );


    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK) // 메인 트랜잭션이 롤백된 후 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 새로운 트랜잭션에서 실행
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.info("PaymentFailedEvent 수신: paymentId={}, userId={}, reservationId={}, seatId={}, errorCode={}",
                event.paymentId(), event.userId(), event.reservationId(), event.seatId(), event.errorCode());

        try {
            // 0. 결제 상태 FAILED로 변경
            Payment payment = paymentRepository.findById(event.paymentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND, "결제 정보를 찾을 수 없어 상태 변경 실패"));
            paymentRepository.save(payment.fail());
            log.info("결제 상태 FAILED 변경 완료: paymentId={}", event.paymentId());

            // 1. 사용자 잔액 복원 (조건부 처리)
            if (!PRE_CHARGE_ERRORS.contains(event.errorCode())) {
                User user = userRepository.findById(event.userId())
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없어 잔액 복원 실패"));
                User refundedUser = user.refund(event.amount());
                userRepository.save(refundedUser);
                log.info("사용자 잔액 복원 완료: userId={}, restoredAmount={}", refundedUser.id(), event.amount());
            } else {
                log.info("잔액 차감 전 오류 발생({}). 잔액 복원 로직을 건너뜁니다. userId={}", event.errorCode(), event.userId());
            }

            // 2. 예약 상태 롤백 (FAILED)
            Reservation reservation = reservationRepository.findById(event.reservationId())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND, "예약을 찾을 수 없어 상태 롤백 실패"));
            Reservation rolledBackReservation = reservation.fail();
            reservationRepository.save(rolledBackReservation);
            log.info("예약 상태 롤백 완료: reservationId={}, status={}", rolledBackReservation.id(), rolledBackReservation.status());

            // 3. 좌석 상태 롤백 (AVAILABLE)
            Seat seat = seatRepository.findById(event.seatId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, "좌석을 찾을 수 없어 상태 롤백 실패"));
            Seat rolledBackSeat = seat.fail();
            seatRepository.save(rolledBackSeat);
            log.info("좌석 상태 롤백 완료: seatId={}, status={}", rolledBackSeat.id(), rolledBackSeat.status());

        } catch (CustomException e) {
            log.error("PaymentFailedEvent 처리 중 비즈니스 예외 발생: {}", e.getErrorCode().name(), e);
        } catch (Exception e) {
            log.error("PaymentFailedEvent 처리 중 예상치 못한 예외 발생", e);
        }
    }
}
