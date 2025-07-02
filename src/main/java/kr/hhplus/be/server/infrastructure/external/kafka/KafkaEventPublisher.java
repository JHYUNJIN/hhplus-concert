package kr.hhplus.be.server.infrastructure.external.kafka;

import kr.hhplus.be.server.application.event.EventPublisher;
import kr.hhplus.be.server.domain.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaEventPublisher implements EventPublisher {

    @Override
    public <T extends Event> void publish(T event) {
        log.info("{} 이벤트 발생", event.getTopic());
    }
}
