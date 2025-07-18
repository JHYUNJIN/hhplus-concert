package kr.hhplus.be.server.external.kafka;

import kr.hhplus.be.server.common.event.Event;
import kr.hhplus.be.server.common.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public <T extends Event> void publish(T event) {
        String topic = event.getTopic().getTopicName();
        String key = event.getKey();
        log.info("카프카 이벤트 발행 시작. Topic: {}, Key: {}", topic, key);
        try {
            // ⭐️ KafkaTemplate을 사용하여 지정된 토픽으로 이벤트 전송
            kafkaTemplate.send(topic, key, event);
            log.info("카프카 이벤트 발행 성공. Topic: {}, Key: {}", topic, key);
        } catch (Exception e) {
            log.error("카프카 이벤트 발행 실패. Topic: {}, Key: {}", topic, key, e);
            // TODO: 발행 실패 시 재시도 또는 실패 로깅/알림 처리
        }
    }

}