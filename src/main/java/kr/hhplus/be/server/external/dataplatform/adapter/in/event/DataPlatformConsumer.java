package kr.hhplus.be.server.external.dataplatform.adapter.in.event;

import kr.hhplus.be.server.external.dataplatform.port.in.PaymentSuccessUseCase;
import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataPlatformConsumer {

    private final PaymentSuccessUseCase paymentSuccessUseCase;

    /**
     * 'payment.success' 토픽을 구독하여 결제 성공 이벤트를 수신하고,
     * 데이터 플랫폼으로 전송하는 유즈케이스를 호출합니다.
     * @param event 카프카로부터 수신한 결제 성공 이벤트
     */
    @KafkaListener(topics = "payment.success", groupId = "${spring.kafka.consumer.group-id.data-platform}")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("DataPlatformConsumer, 결제 성공 이벤트 수신 (Kafka). 데이터 플랫폼 전송을 시작합니다. Event: {}", event);
        try {
            // ⭐️ Consumer는 외부 이벤트 객체에서 내부 시스템에 필요한 데이터를 추출하여
            //    유즈케이스에 전달하는 책임만을 가집니다. (Anti-Corruption Layer)
            paymentSuccessUseCase.sendDataPlatform(
                    event.reservation().id(),
                    event.payment().id(),
                    event.seat().id(),
                    event.seat().concertDateId()
            );
        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 처리 중 오류 발생. Event: {}", event, e);
            // TODO: 실패한 메시지 재처리(Retry) 또는 Dead Letter Queue(DLQ)로 전송
        }
    }
}
