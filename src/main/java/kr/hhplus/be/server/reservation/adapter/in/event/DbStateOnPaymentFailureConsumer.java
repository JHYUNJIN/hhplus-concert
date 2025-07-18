//// 역할: 결제 실패 시 DB 데이터의 상태를 복원하는 책임을 가집니다.
//package kr.hhplus.be.server.reservation.adapter.in.event;
//
//import kr.hhplus.be.server.common.exception.CustomException;
//import kr.hhplus.be.server.common.exception.ErrorCode;
//import kr.hhplus.be.server.concert.domain.Seat;
//import kr.hhplus.be.server.concert.port.out.SeatRepository;
//import kr.hhplus.be.server.payment.domain.Payment;
//import kr.hhplus.be.server.payment.domain.PaymentFailedEvent;
//import kr.hhplus.be.server.payment.port.out.PaymentRepository;
//import kr.hhplus.be.server.reservation.domain.Reservation;
//import kr.hhplus.be.server.reservation.port.out.ReservationRepository;
//import kr.hhplus.be.server.user.domain.User;
//import kr.hhplus.be.server.user.port.out.UserRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Set;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DbStateOnPaymentFailureConsumer { // ⭐️ 클래스 이름을 Consumer로 변경
//
//    private final UserRepository userRepository;
//    private final ReservationRepository reservationRepository;
//    private final PaymentRepository paymentRepository;
//    private final SeatRepository seatRepository;
//
//    private static final Set<ErrorCode> PRE_CHARGE_ERRORS = Set.of(
//            ErrorCode.INSUFFICIENT_BALANCE, ErrorCode.INVALID_PAYMENT_AMOUNT, ErrorCode.ALREADY_PAID,
//            ErrorCode.ALREADY_PROCESSED, ErrorCode.RESERVATION_NOT_FOUND, ErrorCode.PAYMENT_NOT_FOUND,
//            ErrorCode.SEAT_NOT_FOUND, ErrorCode.SEAT_NOT_HOLD, ErrorCode.USER_NOT_FOUND, ErrorCode.INVALID_QUEUE_TOKEN
//    );
//
//    /**
//     * 'payment.failed' 토픽을 구독하여 결제 실패 이벤트를 수신하고,
//     * DB 상태를 복원하는 보상 트랜잭션을 실행합니다.
//     * @param event 카프카로부터 수신한 결제 실패 이벤트
//     */
//    @KafkaListener(topics = "payment.failed", groupId = "${spring.kafka.consumer.group-id}")
//    @Transactional(propagation = Propagation.REQUIRES_NEW) // ⭐️ 메시지 처리를 위한 새로운 트랜잭션 시작
//    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
//        log.info("PaymentFailedEvent 수신 (Kafka). DB 데이터 정리 작업을 시작합니다. Event: {}", event);
//
//        try {
//            Payment payment = paymentRepository.findById(event.paymentId())
//                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND, "결제 정보를 찾을 수 없어 상태 변경 실패"));
//            paymentRepository.save(payment.fail());
//            log.info("결제 상태 FAILED 변경 완료: paymentId={}", event.paymentId());
//
//            if (!PRE_CHARGE_ERRORS.contains(event.errorCode())) {
//                User user = userRepository.findById(event.userId())
//                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없어 잔액 복원 실패"));
//                userRepository.save(user.refund(event.amount()));
//                log.info("사용자 잔액 복원 완료: userId={}, restoredAmount={}", user.id(), event.amount());
//            } else {
//                log.info("잔액 차감 전 오류 발생({}). 잔액 복원 로직을 건너뜁니다. userId={}", event.errorCode(), event.userId());
//            }
//
//            Reservation reservation = reservationRepository.findById(event.reservationId())
//                    .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND, "예약을 찾을 수 없어 상태 롤백 실패"));
//            reservationRepository.save(reservation.fail());
//            log.info("예약 상태 롤백 완료: reservationId={}, status={}", reservation.id(), reservation.status());
//
//            Seat seat = seatRepository.findById(event.seatId())
//                    .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND, "좌석을 찾을 수 없어 상태 롤백 실패"));
//            seatRepository.save(seat.expire());
//            log.info("좌석 상태 롤백 완료: seatId={}, status={}", seat.id(), seat.status());
//
//        } catch (Exception e) {
//            log.error("PaymentFailedEvent DB 처리 중 예외 발생. Event: {}", event, e);
//            // TODO: 실패한 메시지 재처리(Retry) 또는 Dead Letter Queue(DLQ)로 전송
//        }
//    }
//}