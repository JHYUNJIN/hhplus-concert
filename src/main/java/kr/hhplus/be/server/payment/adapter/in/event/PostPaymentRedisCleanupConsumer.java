package kr.hhplus.be.server.payment.adapter.in.event;

import kr.hhplus.be.server.payment.domain.PaymentFailedEvent;
import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;
import kr.hhplus.be.server.queue.port.in.QueueTokenExpirationUseCase;
import kr.hhplus.be.server.reservation.port.in.SeatHoldReleaseUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostPaymentRedisCleanupConsumer {

    private final SeatHoldReleaseUseCase seatHoldReleaseUseCase;
    private final QueueTokenExpirationUseCase queueTokenExpirationUseCase;

    /**
     * 결제 성공 이벤트를 수신하여, Redis 데이터를 정리합니다.
     * @param event 카프카로부터 수신한 결제 성공 이벤트
     */
    @KafkaListener(topics = "payment.success", groupId = "${spring.kafka.consumer.group-id.post-payment-cleanup}")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        try {
            // 1. Redis 좌석 임시 점유(hold) 해제
            seatHoldReleaseUseCase.releaseHold(event.seat().id(), event.user().id());
            // 2. 대기열 토큰 만료 처리
            queueTokenExpirationUseCase.expiresQueueToken(event.queueToken().tokenId().toString());
        } catch (Exception e) {
            log.error("결제 성공 후 Redis 데이터 정리 중 오류 발생. Event: {}", event, e);
            // TODO: 실패한 메시지 재처리(Retry) 또는 Dead Letter Queue(DLQ)로 전송
        }
    }

    /**
     * 결제 실패 이벤트를 수신하여, Redis 데이터를 정리합니다.
     * @param event 카프카로부터 수신한 결제 실패 이벤트
     */
    @KafkaListener(topics = "payment.failed", groupId = "${spring.kafka.consumer.group-id.post-payment-cleanup}")
    public void handlePaymentFailure(PaymentFailedEvent event) {
        try {
            // 1. Redis 좌석 임시 점유(hold) 해제
            seatHoldReleaseUseCase.releaseHold(event.seatId(), event.userId()); // ⭐️ 호출 대상 변경
            // 2. 대기열 토큰 만료 처리
            queueTokenExpirationUseCase.expiresQueueToken(event.tokenId().toString());
        } catch (Exception e) {
            log.error("결제 실패 후 Redis 데이터 정리 중 오류 발생. Event: {}", event, e);
            // TODO: 실패한 메시지 재처리(Retry) 또는 Dead Letter Queue(DLQ)로 전송
        }
    }
}