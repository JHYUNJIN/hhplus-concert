package kr.hhplus.be.server.payment.adapter.in.event;

import kr.hhplus.be.server.payment.domain.PaymentFailedEvent;
import kr.hhplus.be.server.payment.port.in.PaymentFailureCompensationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailureCompensationConsumer {

    private final PaymentFailureCompensationUseCase paymentFailureCompensationUseCase;

    @KafkaListener(topics = "payment.failed", groupId = "${spring.kafka.consumer.group-id.payment-compensator}")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("결제 실패 이벤트 수신 (Kafka). 전체 보상 트랜잭션을 시작합니다. Event: {}", event);
        try {
            // Consumer는 외부 이벤트 객체에서 내부 시스템에 필요한 데이터를 추출하여
            // 유즈케이스(서비스)에 전달하는 책임만 가집니다. (Anti-Corruption Layer)
            paymentFailureCompensationUseCase.compensate(
                    event.paymentId(),
                    event.userId(),
                    event.reservationId(),
                    event.seatId(),
                    event.concertDateId(),
                    event.tokenId().toString(),
                    event.amount(),
                    event.errorCode()
            );
        } catch (Exception e) {
            log.error("결제 실패 보상 처리 중 오류 발생. Event: {}", event, e);
            // TODO: 실패한 메시지 재처리(Retry) 또는 Dead Letter Queue(DLQ)로 전송
        }
    }
}
