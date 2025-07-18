//// 결제 실패 트랜잭션이 롤백된 후, Redis에 남아있는 데이터의 정합성을 맞추는 책임을 가집니다.
//package kr.hhplus.be.server.reservation.adapter.in.event;
//
//import kr.hhplus.be.server.payment.domain.PaymentFailedEvent;
//import kr.hhplus.be.server.queue.port.out.QueueTokenRepository;
//import kr.hhplus.be.server.reservation.port.out.SeatHoldRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.event.TransactionPhase;
//import org.springframework.transaction.event.TransactionalEventListener;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class RedisCleanupOnPaymentFailureConsumer { // 클래스 이름을 변경하여 책임을 명확히 합니다.
//
//    private final QueueTokenRepository queueTokenRepository;
//    private final SeatHoldRepository seatHoldRepository;
//
//    /**
//     * 결제 실패 트랜잭션이 '롤백된 이후'에만 실행되어 데이터 정합성을 보장합니다.
//     * 이 리스너는 오직 Redis 데이터(좌석 점유, 대기열 토큰)를 정리하는 책임만 가집니다.
//     * @param event 결제 실패 이벤트
//     */
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
//    public void handlePaymentFailed(PaymentFailedEvent event) {
//        log.info("PaymentFailedEvent 수신. Redis 데이터 정리 작업을 시작합니다. UserId: {}, SeatId: {}", event.userId(), event.seatId());
//
//        try {
//            // 1. 좌석 임시 점유(hold) 정보를 Redis에서 삭제합니다.
//            seatHoldRepository.deleteHold(event.seatId(), event.userId());
//            log.info("좌석 점유(hold) 해제 완료. SeatId: {}", event.seatId());
//
//            // 2. 사용자의 활성 토큰을 만료 처리합니다.
//            queueTokenRepository.expiresQueueToken(event.tokenId().toString());
//            log.info("대기열 토큰 만료 처리 완료. TokenId: {}", event.tokenId());
//
//        } catch (Exception e) {
//            // 후처리 작업 실패 시, 별도의 모니터링이나 재시도 로직을 위해 로그를 남깁니다.
//            log.error("결제 실패 후 Redis 데이터 정리 작업 중 오류 발생. Event: {}", event, e);
//        }
//    }
//}