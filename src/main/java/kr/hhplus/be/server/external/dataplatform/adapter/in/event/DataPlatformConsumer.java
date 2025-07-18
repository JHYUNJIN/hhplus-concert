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

    // ⭐️ 구체 클래스(DataPlatformService) 대신 인터페이스(PaymentSuccessUseCase)를 주입받습니다.
    // 이렇게 하면 Spring이 생성한 프록시 객체와 타입이 일치하여 의존성 주입이 성공합니다.
    private final PaymentSuccessUseCase paymentSuccessUseCase;

    /**
     * 'payment.success' 토픽을 구독하여 결제 성공 이벤트를 수신하고,
     * 데이터 플랫폼으로 전송하는 유즈케이스를 호출합니다.
     * @param event 카프카로부터 수신한 결제 성공 이벤트
     */
    // ⭐️ groupId를 별도로 지정하여, 다른 컨슈머와 독립적으로 메시지를 수신하도록 합니다.
    @KafkaListener(topics = "payment.success", groupId = "data-platform-group")
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("DataPlatformConsumer, 결제 성공 이벤트 수신 (Kafka). 데이터 플랫폼 전송을 시작합니다. Event: {}", event);
        try {
            // 인터페이스의 메소드를 호출합니다. 실제 구현은 DataPlatformService가 담당합니다.
            paymentSuccessUseCase.sendDataPlatform(event);
        } catch (Exception e) {
            log.error("데이터 플랫폼 전송 처리 중 오류 발생. Event: {}", event, e);
            // TODO: 실패한 메시지 재처리(Retry) 또는 Dead Letter Queue(DLQ)로 전송
        }
    }
}


//// 1. build.gradle에 Spring Retry를 위한 의존성을 추가해야 합니다.
//// implementation 'org.springframework.boot:spring-boot-starter-aop'
//
//// 2. 애플리케이션 메인 클래스에 @EnableRetry 어노테이션을 추가해야 합니다.
//// @EnableRetry
//// @SpringBootApplication
//// public class ServerApplication { ... }
//
//
//// 3. DataPlatformConsumer.java (수정본)
//package kr.hhplus.be.server.dataplatform.adapter.in.event;
//
//import kr.hhplus.be.server.dataplatform.port.in.PaymentSuccessUseCase;
//import kr.hhplus.be.server.payment.domain.PaymentSuccessEvent;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.retry.annotation.Backoff;
//import org.springframework.retry.annotation.Recover;
//import org.springframework.retry.annotation.Retryable;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DataPlatformConsumer {
//
//    private final PaymentSuccessUseCase paymentSuccessUseCase;
//    private final KafkaTemplate<String, Object> kafkaTemplate; // ⭐️ DLQ 전송을 위해 주입
//
//    private static final String DLQ_TOPIC = "payment.success.dlq";
//
//    /**
//     * 'payment.success' 토픽을 구독합니다.
//     * 데이터 전송 실패 시, 최대 3번까지 재시도합니다. (초기 2초, 이후 2배씩 지연 시간 증가)
//     * @param event 카프카로부터 수신한 결제 성공 이벤트
//     */
//    @Retryable(
//            value = { Exception.class }, // 모든 예외에 대해 재시도
//            maxAttempts = 3, // 최대 3번 시도
//            backoff = @Backoff(delay = 2000, multiplier = 2) // 2초, 4초 간격으로 재시도
//    )
//    @KafkaListener(topics = "payment.success", groupId = "data-platform-group")
//    public void handlePaymentSuccess(PaymentSuccessEvent event) {
//        log.info("결제 성공 이벤트 수신 (Kafka). 데이터 플랫폼 전송을 시작합니다. Event: {}", event);
//        // 인터페이스의 메소드를 호출합니다. 실제 구현은 DataPlatformService가 담당합니다.
//        paymentSuccessUseCase.sendDataPlatform(event);
//    }
//
//    /**
//     * @Retryable에서 지정한 모든 재시도가 실패했을 때 실행되는 복구 메소드입니다.
//     * 실패한 이벤트를 Dead Letter Queue(DLQ) 토픽으로 전송하여, 나중에 분석하고 수동으로 처리할 수 있도록 합니다.
//     * @param e 재시도를 모두 실패하게 만든 마지막 예외
//     * @param event 실패한 원본 이벤트 메시지
//     */
//    @Recover
//    public void recover(Exception e, PaymentSuccessEvent event) {
//        log.error("최종 재시도 실패. 메시지를 DLQ로 전송합니다. Event: {}, Error: {}", event, e.getMessage());
//        // 실패한 메시지를 별도의 DLQ 토픽으로 발행합니다.
//        kafkaTemplate.send(DLQ_TOPIC, event.getKey(), event);
//    }
//}
